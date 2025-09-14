package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import lombok.Data;



@Data
public class Logs {

    String timestamp;
    String stage;
    String level;
    String message;


}
