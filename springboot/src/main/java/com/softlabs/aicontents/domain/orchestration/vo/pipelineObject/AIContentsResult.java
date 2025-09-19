package com.softlabs.aicontents.domain.orchestration.vo.pipelineObject;

import lombok.Data;

@Data
public class AIContentsResult {

    //공통
    private int executionId;
    private boolean success;
    private String resultData;
    private String errorMessage;
    private String stepCode;

    //발행 생성에 필요한 응답 객체
    private String title;
    private String summary;
    private String hashtags;
    private String content;
    private String sourceUrl;
    private String aIContentStatusCode;


}
