package com.goormthon.backend.firstsori.domain.board.domain.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.goormthon.backend.firstsori.global.common.exception.ErrorCode;
import com.goormthon.backend.firstsori.global.common.response.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private String saveImage(MultipartFile image) {
        String fileName = image.getOriginalFilename();

        String fileExtension = validateImageFileExtension(fileName);

        try {
            return


                    saveImageToS3(image, fileName, fileExtension);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IO_EXCEPTION_ON_IMAGE_SAVE);
        }

    }

    /**
     * 파일 확장자 확인 하기 (확장자가 jpg, jpeg, png, gif 가 아니면 예외 처리)
     */
    private String validateImageFileExtension(String fileName) {

        int lastDot = fileName.lastIndexOf(".");

        if (lastDot == -1) {
            throw new CustomException(ErrorCode.NO_FILE_EXTENSION);
        }

        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif"};
        String fileExtension = fileName
                .substring(lastDot + 1)
                .toLowerCase();

        if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        return fileExtension;


    }

    /**
     * S3에 이미지 파일 저장하기
     */
    private String saveImageToS3(MultipartFile image,
                                 String originalFilename,
                                 String fileExtension) throws IOException {

        // S3에 저장할 파일 이름을 UUID로 생성 (중복 방지)
        String newS3FileName = UUID.randomUUID().toString().substring(0, 10)
                + "_" + originalFilename;

        InputStream inputStream = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        // Amazon S3에 저장되는 파일 또는 객체와 관련된 정보 (파일의 유형, 크기, 버전 등)
        // 사용자가 정의한 추가적인 정보를 포함할 수 있음. 파일의 작성자, 설명, 태그
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + fileExtension);
        metadata.setContentLength(bytes.length);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            // PutObjectRequest : Amazon S3에 객체를 저장하는 요청
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucket, newS3FileName, byteArrayInputStream, metadata);

            amazonS3.putObject(putObjectRequest);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.PUT_OBJECT_ERROR);
        }

        return amazonS3.getUrl(bucket, newS3FileName).toString();
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
     * 이미지 파일을 S3에 저장 먼저 이미지 파일이 비어있는지 확인 (비어있으면 예외 처리)
     */
    public String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty() || image.getOriginalFilename() == null) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        return saveImage(image);
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
