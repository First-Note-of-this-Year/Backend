package com.goormthon.backend.firstsori.domain.board.domain.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Base64 문자열에서 이미지 타입 추출
     * data:image/png;base64,iVBORw0KG... 형식에서 png 추출
     */
    private String extractImageTypeFromBase64(String base64Image) {
        if (base64Image.startsWith("data:image/")) {
            Pattern pattern = Pattern.compile("data:image/([a-zA-Z]+);base64,");
            Matcher matcher = pattern.matcher(base64Image);
            if (matcher.find()) {
                return matcher.group(1).toLowerCase();
            }
        }
        // 기본값으로 png 반환
        return "png";
    }

    /**
     * Base64 문자열에서 실제 데이터 부분만 추출
     */
    private String extractBase64Data(String base64Image) {
        if (base64Image.contains(",")) {
            return base64Image.split(",")[1];
        }
        return base64Image;
    }

    /**
     * 파일 확장자 유효성 검증
     */
    private void validateImageFileExtension(String fileExtension) {
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"};
        boolean isValid = false;
        
        for (String ext : allowedExtensions) {
            if (ext.equalsIgnoreCase(fileExtension)) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    /**
     * Base64 문자열을 디코딩하여 S3에 저장
     */
    private String saveBase64ImageToS3(String base64Image, String fileExtension) {
        try {
            // Base64 데이터 추출 및 디코딩
            String base64Data = extractBase64Data(base64Image);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // S3에 저장할 파일 이름 생성 (UUID 사용)
            String newS3FileName = UUID.randomUUID().toString().substring(0, 10)
                    + "_image." + fileExtension;

            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/" + fileExtension);
            metadata.setContentLength(imageBytes.length);

            // S3에 업로드
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucket, newS3FileName, byteArrayInputStream, metadata);

            amazonS3.putObject(putObjectRequest);

            return amazonS3.getUrl(bucket, newS3FileName).toString();

        } catch (IllegalArgumentException e) {
            log.error("Base64 디코딩 실패", e);
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        } catch (Exception e) {
            log.error("S3 업로드 실패", e);
            throw new CustomException(ErrorCode.PUT_OBJECT_ERROR);
        }
    }

    /**
     * 이미지 파일의 주소로부터 key 추출
     */
    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String key = URLDecoder.decode(url.getPath(), "UTF-8");
            return key.substring(1);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    /**
     * Base64 인코딩된 이미지 문자열을 받아서 S3에 저장
     * 
     * @param base64Image Base64 인코딩된 이미지 문자열 
     *                    (예: "data:image/png;base64,iVBORw0KG..." 또는 순수 Base64 문자열)
     * @return S3에 저장된 이미지의 URL
     */
    public String uploadImage(String base64Image) {
        // 빈 문자열 체크
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        // 이미지 타입 추출 및 검증
        String fileExtension = extractImageTypeFromBase64(base64Image);
        validateImageFileExtension(fileExtension);

        // S3에 저장
        return saveBase64ImageToS3(base64Image, fileExtension);
    }

    /**
     * 이미지의 Key를 가지고 와서 S3내 객체를 삭제한다
     */
    public void deleteImage(String urlAddress) {
        String key = getKeyFromImageAddress(urlAddress);

        try {
            amazonS3.deleteObject(bucket, key);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }
}