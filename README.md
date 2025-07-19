# StudyLog-BE

### Git Flow 협업 방식
```
1. 원격 저장소를 로컬 컴퓨터에 origin으로 등록합니다.
2. Origin Repository를 로컬 컴퓨터로 clone 및 pull을 합니다.
3. 각자의 이슈 브랜치에서 작업 후 Origin Repository로 push 합니다.
4. Develop 브랜치로 PR을 보낸 후 merge 합니다.
```
### 개발 시작 시
```
1. 새로운 Issue를 생성합니다.
2. develop 브랜치에서 이슈에 대한 브랜치를 생성합니다.
  - (브랜치 이름: [작업 유형]/#[이슈번호]-[작업할 기능])
  - ex) Feature/#1-kakao-login
3. 로컬에서 Fetch를 하여 생성된 브랜치를 반영합니다.
4. 해당 브랜치로 이동(checkout)한 후 작업합니다.
```
#### 이슈 생성 시 주의사항
```
1. 이슈의 제목은 "[작업 유형] 작업할 기능"으로 작성합니다.
  ex) [Feature] 회원가입
2. 개발할 기능과 작업의 상세 내용을 작성합니다.
3. Assignee와 Label을 설정합니다. 
```
### 개발 완료 후
```
1. 개발을 완료하면 Origin Repository로 push합니다.
2. 해당 브랜치에서 develop 브랜치로 리뷰어를 설정하여 PR을 보냅니다.
3. 코드 리뷰 후, 리뷰어가 merge 합니다.
4. merge가 완료되면 로컬에서 develop 브랜치로 이동합니다.
5. 원격 저장소의 develop 브랜치를 로컬에서 pull 합니다.
```
#### PR 메시지 제목 형식
```
[Feature/#(issue-number)] 새로운 기능 추가
[Fix/#(issue-number)] 버그 수정
[Refactor/#(issue-number)] 코드 리팩토링
[Test/#(issue-number)] 테스트 로직
```
