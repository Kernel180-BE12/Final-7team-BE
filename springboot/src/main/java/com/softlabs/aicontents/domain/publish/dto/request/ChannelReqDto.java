package com.softlabs.aicontents.domain.publish.dto.request;


import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelReqDto{
    private Long Id;
    private String provider;
    private String displayName;
    private PublishMode publishMode;
    private String isActive;
    private String secretRef;
    private String apiUrl;
    private ContentFormat contentFormat;
    private String extraJson;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void deactivate(){
        this.isActive = "N";
    }
    public void activate() {
        this.isActive = "Y";
    }

    // --- ENUMS ---
    public enum PublishMode {
        BROWSER, API, MANUAL
    }
    public enum ContentFormat {
        MARKDOWN, HTML, TEXT
    }
}