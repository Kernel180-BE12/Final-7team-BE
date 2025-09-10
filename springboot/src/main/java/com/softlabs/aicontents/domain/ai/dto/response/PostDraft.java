package com.softlabs.aicontents.domain.ai.dto.response;

import java.util.List;

/**
 * FastAPI 응답 JSON과 1:1 매핑.
 */
public class PostDraft {
    public String title;
    public String meta_description;
    public List<String> hashtags;
    public String body_markdown;
    public List<String> evidence;
    public String version;

    public PostDraft() {}
}
