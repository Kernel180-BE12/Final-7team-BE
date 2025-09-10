package com.softlabs.aicontents.domain.ai.controller;

import com.softlabs.aicontents.domain.ai.dto.response.PostDraft;
import com.softlabs.aicontents.domain.ai.dto.request.ProductBrief;
import com.softlabs.aicontents.domain.ai.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test/llm")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // POST /test/llm/generate?product=텀블러
    @PostMapping("/generate")
    public PostDraft generate(@RequestParam(defaultValue = "텀블러") String product) {
        ProductBrief brief = new ProductBrief(
                product,
                "https://ssadagu.kr/...",
                "₩15,900",
                List.of("보냉", "휴대")
        );
        return aiService.generate(brief);
    }
}
