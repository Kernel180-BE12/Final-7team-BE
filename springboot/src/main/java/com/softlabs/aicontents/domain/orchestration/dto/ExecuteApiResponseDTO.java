package com.softlabs.aicontents.domain.orchestration.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;

@Data
public class ExecuteApiResponseDTO {

//@PostMapping("/execute")
    private boolean success;
    private String message;
    private PipeExecuteData pipeExecuteData ;


}
