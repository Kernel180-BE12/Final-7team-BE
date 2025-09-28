package com.softlabs.aicontents.domain.orchestration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.swing.*;

@Data
@Schema(description = "파이프라인 상태 조회 요청 DTO")
public class PipeStatusExcIdReqDTO {

    @Schema(description = "파이프라인 실행 ID",example = "2001", required=true)
    private int executionId;
}
