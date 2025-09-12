package com.softlabs.aicontents.domain.publish.dto.reponse;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PublishResDto {
    private  Long publishId;
    private  Long aiContentId;

    private  String blogPlatform;   // NAVER 등
    private  String blogPostId;     // 외부 글 ID
    private  String blogUrl;        // 외부 글 URL

    private  PublishStatus publishStatus; // SUCCESS / FAILED / PENDING
    private  String publishResponse;      // 원시 응답 JSON
    private  String errorMessage;

    private  Integer attemptCount;
//    private String idempotencyKey;      // 중복방지 키

    private  LocalDateTime publishedAt;
    private  LocalDateTime createdAt;
    private  LocalDateTime updatedAt;

    public enum PublishStatus { SUCCESS, FAILED, PENDING }

    public boolean isSuccess() {
        return publishStatus == PublishStatus.SUCCESS;
    }
    public void increaseAttempt() {
        this.attemptCount = (this.attemptCount == null) ? 1 : this.attemptCount + 1;
    }
}