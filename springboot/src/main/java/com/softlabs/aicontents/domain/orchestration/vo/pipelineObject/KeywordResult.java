package com.softlabs.aicontents.domain.orchestration.vo.pipelineObject;

import lombok.Data;

@Data
public class KeywordResult {
    //크롤링에 필요한 응답 객체
    private int executionId;
    private String keyword;
    private boolean success;
    private String resultData;
    private String errorMessage;
    private String stepCode;
    private String keyWordStatusCode;

}
