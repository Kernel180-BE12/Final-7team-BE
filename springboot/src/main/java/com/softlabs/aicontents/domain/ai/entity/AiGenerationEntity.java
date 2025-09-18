package com.softlabs.aicontents.domain.ai.entity;

import lombok.Data;

@Data
public class AiGenerationEntity {
    private Long genId;
    private Long requestId;
    private String status;            // PENDING/SUCCESS/ERROR

    private String modelName;
    private Double temperature;
    private Double timeoutSec;
    private Integer retries;

    private Double latencyMs;
    private Integer tokensPrompt;
    private Integer tokensCompletion;
    private String fallbackUsed;      // 'Y' / 'N'

    private String errorMessage;

    private String systemMsgSnap;     // CLOB
    private String genPromptSnap;     // CLOB
    private String fixPromptSnap;     // CLOB
}
