package com.softlabs.aicontents.domain.monitoring.dto.response;

public record ActivityEntry(
        String id,
        String title,
        String description,
        String type,
        String timestamp
) {}
