package com.softlabs.aicontents.domain.testDomainService;

import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.testDomain.TestDomainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@Slf4j
public class AIContentService {

    @Autowired
    private TestDomainMapper testDomainMapper;

    public AIContentsResult aIContentsResultExecute(int executionId, ProductCrawlingResult productCrawlingResult) {

        AIContentsResult result = new AIContentsResult();
        result.setExecutionId(executionId);
        result.setStepCode("STEP03");

        try {
            log.info("AI 콘텐츠 생성 시작 - executionId: {}", executionId);

            // DB에서 상품 정보 조회
            AIContentsResult productData = testDomainMapper.selectProductDataByExecutionId(executionId);
            if (productData == null) {
                throw new RuntimeException("executionId " + executionId + "에 해당하는 상품 정보를 찾을 수 없습니다.");
            }

            // 프로토타입 AI 콘텐츠 생성 (하드코딩)
            String productName = productData.getProductName();
            result.setTitle("[AI 생성] " + productName + " 완벽 가이드");
            result.setSummary(productName + "에 대한 상세한 리뷰와 구매 가이드입니다.");
            result.setHashtags("#AI리뷰 #" + productName.substring(0, Math.min(5, productName.length())) + " #구매가이드 #추천상품");
            result.setContent("# " + productName + " AI 분석 리포트\n\n" +
                    "## 상품 개요\n" +
                    "- 제품명: " + productName + "\n" +
                    "- 가격: " + productData.getPrice() + "원\n" +
                    "- 링크: " + productData.getSourceUrl() + "\n\n" +
                    "## AI 분석 결과\n" +
                    "이 상품은 AI 분석 결과 우수한 품질과 경쟁력 있는 가격을 보입니다.\n\n" +
                    "## 구매 추천\n" +
                    "AI 종합 점수: 85/100\n" +
                    "이 제품을 적극 추천드립니다!");
            result.setAIContentStatusCode("SUCCESS");
            result.setSuccess(true);
            result.setResultData("AI 콘텐츠 생성 완료: " + result.getTitle());

            // DB에 저장
            testDomainMapper.insertAIContentResult(
                    executionId,
                    productData.getSourceUrl(),
                    result.getTitle(),
                    result.getContent(),
                    result.getSummary(),
                    result.getHashtags(),
                    result.getAIContentStatusCode()
            );

            log.info("AI 콘텐츠 생성 완료 - executionId: {}, title: {}", executionId, result.getTitle());

        } catch (Exception e) {
            log.error("AI 콘텐츠 생성 중 오류 발생 - executionId: {}", executionId, e);

            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setAIContentStatusCode("FAILED");
            result.setResultData("AI 콘텐츠 생성 실패");

            throw new RuntimeException("AI 콘텐츠 생성 실패", e);
        }

        return result;
    }
}
