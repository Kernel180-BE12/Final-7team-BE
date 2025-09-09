package com.softlabs.aicontents.domain.monitoring.dto.response;

import java.util.List;

public record SystemResponseDTO(
    Status status,
    List<ServiceStatus> service
) {
    public record Status(
            String status
    ){}
    public record ServiceStatus(
            String database,
            String llm,
            String crawler,
            String scheduler,
            String uptime,
            String version
    ){}
}
