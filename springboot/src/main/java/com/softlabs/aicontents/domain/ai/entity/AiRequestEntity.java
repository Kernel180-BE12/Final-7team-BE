package com.softlabs.aicontents.domain.ai.entity;

import lombok.Data;

@Data
public class AiRequestEntity {
    private Long requestId;
    private String productName;
    private String priceStr;
    private String sourceUrl;
    private String requestJson;   // CLOB ↔ String (텍스트만)
    private String requestHash;
    // createdAt은 필요 시 추가
}
