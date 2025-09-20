package com.softlabs.aicontents.domain.orchestration.vo.pipelineObject;

import lombok.Data;

@Data
public class ProductCrawlingResult {

    //공통
    private int executionId;
    private boolean success;
    private String resultData;
    private String errorMessage;
    private String stepCode;

    //LLM 생성에 필요한 응답 객체
    private String productName;
    private String sourceUrl;
    private String price;
    private String productStatusCode;


}
