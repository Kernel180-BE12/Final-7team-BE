package com.softlabs.aicontents.domain.ai.service;

import com.softlabs.aicontents.domain.ai.dto.response.PostDraft;
import com.softlabs.aicontents.domain.ai.dto.request.ProductBrief;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * FastAPI의 /v1/generate/post 에 POST 요청을 보내고
 * 응답을 PostDraft로 받아서 반환.
 */
@Service
public class AiService {
    private final WebClient llmwebClient;

    //생성자로 WebClient Bean 주입
    public AiService(WebClient llmwebClient) {
        this.llmwebClient = llmwebClient;
    }

    public PostDraft generate(ProductBrief brief) {
        return llmwebClient.post()
                .uri("/v1/generate/post")
                .bodyValue(brief) // ProductBrief → JSON
                .retrieve()
                .bodyToMono(PostDraft.class) // JSON → PostDraft
                .block(); // 데모: 동기 블록

    }
}