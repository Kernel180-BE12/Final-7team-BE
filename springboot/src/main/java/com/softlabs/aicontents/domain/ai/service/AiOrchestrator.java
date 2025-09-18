package com.softlabs.aicontents.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.domain.ai.dto.request.ProductBrief;
import com.softlabs.aicontents.domain.ai.dto.response.PostDraft;
import com.softlabs.aicontents.domain.ai.entity.AiGenerationEntity;
import com.softlabs.aicontents.domain.ai.entity.AiPostEntity;
import com.softlabs.aicontents.domain.ai.entity.AiRequestEntity;
import com.softlabs.aicontents.domain.ai.mapper.AiGenerationMapper;
import com.softlabs.aicontents.domain.ai.mapper.AiPostMapper;
import com.softlabs.aicontents.domain.ai.mapper.AiRequestMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AiOrchestrator {
    private final AiService aiService;                   // FastAPI 호출 (이미 있음)
    private final AiRequestMapper aiRequestMapper;
    private final AiGenerationMapper aiGenerationMapper;
    private final AiPostMapper aiPostMapper;
    private final ObjectMapper objectMapper;
    @Transactional
    public Long generateAndSave(ProductBrief brief) {
        // 1) 요청 해시로 idem 보장 (간단 예: name|price|url|keywords)
        String hash = DigestUtils.sha256Hex(
                (brief.product_name + "|" + brief.price + "|" + brief.source_url + "|" + String.join(",", brief.keywords))
                        .getBytes(StandardCharsets.UTF_8));

        AiRequestEntity existing = aiRequestMapper.findByHash(hash);
        Long requestId;
        if (existing == null) {
            AiRequestEntity req = new AiRequestEntity();
            req.setProductName(brief.product_name);
            req.setPriceStr(brief.price);
            req.setSourceUrl(brief.source_url);
            try {
                req.setRequestJson(objectMapper.writeValueAsString(brief));
            } catch (JsonProcessingException e) {
                throw new SerializationException("Failed to serialize ProductBrief", e);
            }
            req.setRequestHash(hash);
            aiRequestMapper.insertRequest(req);
            requestId = req.getRequestId();
        } else {
            requestId = existing.getRequestId();
        }

        // 2) generation row (PENDING)
        AiGenerationEntity gen = new AiGenerationEntity();
        gen.setRequestId(requestId);
        gen.setStatus("PENDING");
        gen.setModelName("fastapi:" + "v1/generate/post");
        gen.setTemperature(0.2);
        gen.setTimeoutSec(20.0);
        gen.setRetries(2);
        aiGenerationMapper.insertGeneration(gen);

        long t0 = System.currentTimeMillis();
        try {
            // 3) FastAPI 호출
            PostDraft draft = aiService.generate(brief);
            long latency = System.currentTimeMillis() - t0;

            // 4) 성공 표시
            gen.setGenId(gen.getGenId()); // (MyBatis selectKey로 채워짐)
            gen.setLatencyMs((double) latency);
            gen.setTokensPrompt(0);       // 필요시 FastAPI에서 넘겨주면 반영
            gen.setTokensCompletion(0);
            gen.setFallbackUsed("N");
            aiGenerationMapper.markSuccess(gen);

            // 5) 결과 저장
            AiPostEntity post = new AiPostEntity();
            post.setGenId(gen.getGenId());
            post.setTitle(draft.title);
            post.setMetaDescription(draft.meta_description);
            post.setBodyMarkdown(draft.body_markdown);
            post.setHashtagsCsv(String.join(",", draft.hashtags));
            post.setEvidenceCsv(String.join(",", draft.evidence));
            post.setSchemaVersion(draft.version);
            aiPostMapper.insertPost(post);

            return post.getPostId();
        } catch (Exception e) {
            aiGenerationMapper.markError(gen.getGenId(), e.getMessage());
            throw e;
        }
    }
}
