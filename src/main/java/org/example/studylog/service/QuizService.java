package org.example.studylog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.example.studylog.client.ChatGptClient;
import org.example.studylog.dto.CategoryDTO;
import org.example.studylog.dto.quiz.CreateQuizRequestDTO;
import org.example.studylog.dto.quiz.QuizListResponseDTO;
import org.example.studylog.dto.quiz.QuizResponseDTO;
import org.example.studylog.dto.quiz.QuizSummaryDTO;
import org.example.studylog.dto.quiz.chatGPT.ChatGptRequest;
import org.example.studylog.dto.quiz.chatGPT.ChatGptResponse;
import org.example.studylog.entity.StudyRecord;
import org.example.studylog.entity.category.Category;
import org.example.studylog.entity.quiz.Quiz;
import org.example.studylog.entity.quiz.QuizLevel;
import org.example.studylog.entity.quiz.QuizType;
import org.example.studylog.entity.user.User;
import org.example.studylog.exception.BusinessException;
import org.example.studylog.exception.ErrorCode;
import org.example.studylog.repository.CategoryRepository;
import org.example.studylog.repository.QuizRepository;
import org.example.studylog.repository.StudyRecordRepository;
import org.example.studylog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final StudyRecordRepository studyRecordRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;

    private final ChatGptClient chatGptClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<QuizResponseDTO> createQuiz(String oauthId, Long recordId, CreateQuizRequestDTO requestDTO) {
        // 유저 조회
        User user = userRepository.findByOauthId(oauthId);

        // 퀴즈를 만들 기록 조회
        StudyRecord studyRecord = studyRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_RECORD_NOT_FOUND));
        // 이미 퀴즈가 있는 기록이면 에러 반환
//        if(studyRecord.isQuizCreated())
//            throw new BusinessException(ErrorCode.QUIZ_ALREADY_EXISTS);
        if (Boolean.TRUE.equals(studyRecord.isQuizCreated())) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_EXISTS);
        }

        // 기록에 대한 카테고리 조회 (추후 데이터 반환 시 필요함)
        Category category = studyRecord.getCategory();

        // 프롬프트 생성
        String prompt_system = buildSystemPrompt(requestDTO.getLevel().getLabel());
        String prompt_user = buildUserPrompt(requestDTO, studyRecord.getContent());

        // GPT에게 보낼 요청 생성
        ChatGptRequest request = new ChatGptRequest(
                "gpt-4o-mini",
                List.of(new ChatGptRequest.Message("system", prompt_system),
                        new ChatGptRequest.Message("user", prompt_user)),
                0.7,
                0.7
        );

        // GPT에게 요청 보내고 응답 받기
        ChatGptResponse response = chatGptClient.getChatCompletions(request);
        String json = response.getChoices().get(0).getMessage().getContent();

        // 응답 받은 문자열을 키-값 쌍으로 파싱
        List<Map<String, String>> quizList;
        try {
            quizList = objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("퀴즈 파싱 실패: " + json, e);
        }

        // 퀴즈 목록을 DB에 저장
        List<Quiz> quizzes = quizList.stream().map(map -> {
            Quiz q = new Quiz();
            q.setQuestion(map.get("question"));
            q.setAnswer(map.get("answer"));
            q.setLevel(QuizLevel.fromLabel(map.get("level")));
            q.setType(QuizType.valueOf(map.get("type")));
            q.setCreatedAt(studyRecord.getCreateDate());
            q.setRecord(studyRecord);
            q.setUser(user);
            q.setCategory(studyRecord.getCategory());
            return q;
        }).toList();

        quizRepository.saveAll(quizzes);

        // 퀴즈 생성되었으니 '기록'의 isQuizCreated를 true로 설정
        studyRecord.setQuizCreated(true);

        // 응답 데이터 생성
        List<QuizResponseDTO> quizResponseList = quizzes.stream().map(map -> {
            QuizResponseDTO dto = QuizResponseDTO.from(map, category);
            return dto;
        }).toList();

        return quizResponseList;
    }

    private String buildSystemPrompt(String level) {
        List<String> MID_KEYWORDS = Arrays.asList(
                "언제", "상황", "조건", "사례", "선택", "방법", "절차", "방식", "기준", "요건"
        );

        List<String> HIGH_KEYWORDS = Arrays.asList(
                "차이", "왜", "예외", "한계", "내부", "구조", "원리", "특징", "문제점", "비교", "영향"
        );
        String common = (
                "역할: 퀴즈 생성 도우미(한국어). 출력은 오직 JSON 배열.\n" +
                        "스키마:\n" +
                        "[\n" +
                        "    {\"question\":\"...\", \"answer\":\"...\", \"type\":\"OX|SHORT_ANSWER\", \"level\":\"하|중|상\"}\n" +
                        "]\n" +
                        "공통 규칙:\n" +
                        "- 존댓말 사용.\n" +
                        "- answer=SHORT_ANSWER이면, 어미 제거 후 답만 반환\n" +
                        "- 모든 항목의 level은 '" + level + "' 이어야 한다(다른 값 금지).\n" +
                        "- 출력은 반드시 대괄호로 시작/끝나는 순수 JSON 배열. 어떤 객체/키로도 감싸지 말 것.\n" +
                        "- 마크다운/설명/주석/코드블록 금지."
        );

        String body;

        if ("하".equals(level)) {
            String forbidden = Stream.concat(MID_KEYWORDS.stream(), HIGH_KEYWORDS.stream())
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(", "));
            body = (
                    "레벨 규칙(하):\n" +
                            "- type=OX 만 허용.\n" +
                            "- question: 진술형 한 문장, 물음표 금지.\n" +
                            "- question에 다음 단어 금지: [" + forbidden + "]\n" +
                            "- answer는 반드시 \"O\" 또는 \"X\"."
            );
        } else if ("중".equals(level)) {
            String keywords = MID_KEYWORDS.stream()
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(", "));
            body = (
                    "레벨 규칙(중):\n" +
                            "- type=SHORT_ANSWER 만 허용.\n" +
                            "- question에는 반드시 다음 키워드 중 하나 이상 포함: [" + keywords + "]\n" +
                            "- 반드시 '?'로 끝나야 함.\n" +
                            "- answer ≤ 20자."
            );
        } else { // "상"
            String keywords = HIGH_KEYWORDS.stream()
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(", "));
            body = (
                    "레벨 규칙(상):\n" +
                            "- type=SHORT_ANSWER 만 허용.\n" +
                            "- question에는 반드시 다음 키워드 중 하나 이상 포함: [" + keywords + "]\n" +
                            "- 반드시 '?'로 끝나야 함.\n" +
                            "- answer ≤ 40자.\n" +
                            "- answer는 질문의 키워드와 직접적으로 대응되는 설명을 포함해야 하며, 단순 키워드 나열 금지.\n" +
                            "- answer는 최소 2가지 이상의 구체적 포인트(예: 구조적 특징 2개, 한계 2개)를 포함해야 함."
            );
        }

        return common + "\n" + body;

    }

    private String buildUserPrompt(CreateQuizRequestDTO dto, String content) {
        return String.format("""
                생성 조건
                - 주제: %s
                - 난이도: %s
                - 개수: %d
                %s
                레벨 규칙과 스키마를 반드시 준수.
                """,
                content,
                dto.getLevel().getLabel(),
                dto.getQuizCount(),
                dto.getRequirement() != null ? "- 참고사항: " + dto.getRequirement() : "");
    }

    private String buildPrompt(CreateQuizRequestDTO dto, String content){
        return String.format("""
                        너는 퀴즈 생성 도우미야. 아래 내용을 바탕으로 퀴즈를 만들어줘.
                                        
                        생성 조건
                        - 주제: %s
                        - 난이도: %s
                        - 개수: %d개
                        - 문제 유형: OX 또는 단답형 중에서 무작위로 섞어서 생성해줘.
                        %s
                                        
                        참고사항은 문제 유형보다 **우선되는 강력한 조건**이야. 예를 들어 "단답형으로만 생성"이라는 참고사항이 있다면, 문제 유형은 무조건 단답형이여야 해.
                                        
                        출력 형식은 JSON 배열로 다음 구조를 따라줘. `type`은 반드시 대문자로 작성해.
                                        
                        📌 난이도별 문제 생성 기준 (매우 중요 — 반드시 따를 것):
                           - 하: 기초 정의, 용어 설명, 개념 암기 문제 (ex. "~이란?", "~의 정의는?")
                           - 중: 개념 응용, 사례 분석, 실제 사용 예를 묻는 문제 (ex. "~을 언제 사용하나요?", "예시 중 올바른 것을 고르세요")
                           - 상: 비교, 한계, 내부 동작 원리, 예외 상황 등 심화 개념을 묻는 문제 (ex. "~와 ~의 차이는?", "왜 ~한가요?", "다음 중 틀린 설명은?")
                                
                           → 난이도에 따라 문제의 **내용 자체가 달라야 하며**, 단순히 문장 표현만 다르게 만들면 안 된다.
                           → 난이도가 높을수록 더 깊은 이해가 필요한 질문이 되도록 구성하라.
                           → 동일한 내용을 표현만 다르게 반복하지 마. 각 난이도별로 주제의 다른 측면을 다뤄야 해.
                        [
                          {
                            "question": "...",
                            "answer": "...",
                            "type": "OX" 또는 "SHORT_ANSWER",
                            "level": 내가 제시한 난이도 그대로
                          }
                        ]
                                        
                        반드시 지켜야 할 작성 규칙
                        - question에는 절대 문제 유형(O/X 등)을 포함하지 마. 단순한 문제 내용만 적어.
                          ❌ 예: "다음 설명이 맞으면 O, 틀리면 X를 고르시오."
                          ❌ 예: "스프링 빈은 ~이다. (O/X)"
                          ✅ 예: "스프링 빈이란?"
                        - answer에는 정확한 정답을 적어. OX의 경우 "O" 또는 "X"로, 단답형은 문장이나 단어로 답변해.
                        - level은 내가 준 난이도를 그대로 넣어 (예: "하", "중", "상").
                        - SHORT_ANSWER 유형의 answer는 반드시 20자 이내로 작성해야 해. (한 줄 요약 형태)
                          예: "빈은 스프링이 관리하는 객체"
                          ❌ 예: "XML 설정 파일, 자바 기반 설정 클래스..."
                        - 단답형은 '무엇인가요?', '이유는?', '용도는?' 등 간결한 질문으로 구성해.
                        - "나열해보세요", "설명하세요", "작성해보세요" 같은 표현은 사용하지 마. (긴 답변 유도 금지)
                                        
                        출력은 JSON 배열로만 응답해. 설명이나 여는 말 없이 JSON만 반환해줘.
                """,
                content,
                dto.getLevel().getLabel(),
                dto.getQuizCount(),
                dto.getRequirement() != null ? "- 참고사항: " + dto.getRequirement() : ""
        );
    }

    @Transactional(readOnly = true)
    public QuizResponseDTO getQuiz(String oauthId, Long quizId) {
        // 유저 조회
        User user = userRepository.findByOauthId(oauthId);

        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈가 존재하지 않습니다."));

        // 해당 퀴즈가 로그인한 사용자의 것인지 확인
        if(user != quiz.getUser())
            throw new IllegalArgumentException("현재 유저의 퀴즈가 아닙니다.");

        // 퀴즈의 카테고리 가져오기
        Category category = quiz.getCategory();

        // 응답 데이터 생성
        QuizResponseDTO dto = QuizResponseDTO.from(quiz, category);

        return dto;
    }

    @Transactional(readOnly = true)
    public QuizListResponseDTO getQuizList(String oauthId, String query, Long lastId, int size, LocalDate date, Long categoryId) {
        // 로그인한 유저 조회
        User user = userRepository.findByOauthId(oauthId);

        // 조건에 맞는 퀴즈 목록 조회
        List<Quiz> quizzes = quizRepository.findQuizzes(user.getId(), lastId, size + 1, date, categoryId, query);

        // 유저의 카테고리 조회
        List<Category> categories = categoryRepository.findByUserOrderByNameAsc(user);

        // 다음 데이터 존재 여부
        boolean hasNext = quizzes.size() > size;
        if (hasNext) quizzes.remove(size);   // 커서 확인용으로만 사용한 마지막 1개 제거

        return QuizListResponseDTO.builder()
                .categories(categories.stream().map(CategoryDTO::from).toList())
                .quizzes(quizzes.stream().map(QuizSummaryDTO::from).toList())
                .hasNext(hasNext)
                .lastId(quizzes.isEmpty() ? null : quizzes.get(quizzes.size()-1).getId())
                .build();
    }
}
