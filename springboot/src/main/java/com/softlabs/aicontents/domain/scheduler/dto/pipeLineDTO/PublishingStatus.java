package com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO;

import lombok.Data;

@Data
public class PublishingStatus {
    String platform;
    String status;
    String url;
}
