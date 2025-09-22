package com.softlabs.aicontents.domain.orchestration.vo.pipelineObject;

import lombok.Data;

@Data
public class KeywordResult {

    //공통
    private int executionId;
    private boolean success;
    private String resultData;
    private String errorMessage;
    private String stepCode;

    //크롤링에 필요한 응답 객체
    private String keyword;
    private String keyWordStatusCode;

}
