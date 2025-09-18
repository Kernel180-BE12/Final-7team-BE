package com.softlabs.aicontents.domain.ai.controller;

//import com.softlabs.aicontents.domain.ai.dto.response.PostDraft;
import com.softlabs.aicontents.domain.ai.dto.request.ProductBrief;
import com.softlabs.aicontents.domain.ai.service.AiOrchestrator;
import com.softlabs.aicontents.domain.ai.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test/llm")
public class AiController {
    private final AiService aiService;
    private final AiOrchestrator aiOrchestrator;

    public AiController(AiService aiService, AiOrchestrator aiOrchestrator) {
        this.aiService = aiService;
        this.aiOrchestrator = aiOrchestrator;
    }

    // ✅ 권장: Postman Body(JSON)를 그대로 받아 처리
    @PostMapping("/generate/save")
    public ResponseEntity<Map<String, Object>> generateAndSave(@RequestBody ProductBrief brief) {
        Long postId = aiOrchestrator.generateAndSave(brief);
        return ResponseEntity.ok(Map.of("ok", true, "postId", postId));
    }

    // POST /test/llm/generate?product=텀블러
//    @PostMapping("/generate")
//    public PostDraft generate(@RequestParam(defaultValue = "텀블러") String product) {
//        ProductBrief brief = new ProductBrief(
//                product,
//                "https://ssadagu.kr/...",
//                "₩15,900",
//                List.of("보냉", "휴대")
//        );
//        return aiService.generate(brief);
//    }
//
//    // 새로 추가: 생성 + DB 저장
//    @PostMapping("/generate/save")
//    public Map<String,Object> generateAndSave(@RequestParam(defaultValue = "텀블러") String product) {
//        ProductBrief brief = new ProductBrief(product, "https://ssadagu.kr/...", "₩15,900", List.of("보냉","휴대"));
//        Long postId = aiOrchestrator.generateAndSave(brief);
//        return Map.of("ok", true, "postId", postId);
//    }
}
