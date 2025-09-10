package com.softlabs.aicontents.domain.publish.dto.request;

import lombok.Data;

@Data
public class AicontentsDto {
    private String Title;
    private String MetaDescription;
    private String Markdown;
    private String Hashtag;
}
