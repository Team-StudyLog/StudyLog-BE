package org.example.studylog.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Template s3Template;

    public String uploadFile(MultipartFile file){
        try{
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


}
