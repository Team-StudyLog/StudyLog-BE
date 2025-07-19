package org.example.studylog.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studylog.entity.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Template s3Template;

    public String uploadFile(MultipartFile file, User user){
        try{
            // 프로필 업데이트한 유저면 프로필 이미지 URL에서 객체 키값 추출 후 삭제
            if(user.isProfileCompleted()){
                deleteFileByKey(user.getProfileImage());
            }

            // 파일 이름 고유하게 하기 위해 UUID 사용
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            // 파일 내용을 스트림 형태로 추출
            InputStream stream = file.getInputStream();

            // 파일의 부가 정보 담는 객체 (파일 타입)
            ObjectMetadata metadata = ObjectMetadata.builder().contentType(file.getContentType()).build();

            // 이미지 업로드
            S3Resource s3resource = s3Template.upload(bucket, fileName, stream, metadata);

            return String.valueOf(s3resource.getURL());
        } catch (Exception e){
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    public void deleteFileByKey(String fileUrl){
        if(fileUrl == null || fileUrl.isEmpty()) return;
        String key = getObjectKey(fileUrl);
        s3Template.deleteObject(bucket, key);
    }

    public String getObjectKey(String URL){
        // URL 에서 객체 키값 추출
        String bucketUrl = "https://study-log-1.s3.ap-northeast-2.amazonaws.com/";
        String fileKey = URLDecoder.decode(URL.replace(bucketUrl, ""), StandardCharsets.UTF_8);
        return fileKey;
    }

}
