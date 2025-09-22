package com.softlabs.aicontents.domain.testDomainService;

import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.testDomain.TestDomainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductCrawlingService {

  @Autowired private TestDomainMapper testDomainMapper;

  public ProductCrawlingResult productCrawlingExecute(
      int executionId, KeywordResult keywordResult) {

    ProductCrawlingResult result = new ProductCrawlingResult();
    result.setExecutionId(executionId);
    result.setKeyword(keywordResult.getKeyword());
    result.setStepCode("STEP02");

    try {
      log.info("상품 크롤링 시작 - executionId: {}, keyword: {}", executionId, keywordResult.getKeyword());

      // 프로토타입용 샘플 상품 정보 생성
      ProductInfo sampleProduct = generateSampleProduct(keywordResult.getKeyword());

      // 결과 객체에 설정
      result.setProductName(sampleProduct.getProductName());
      result.setSourceUrl(sampleProduct.getSourceUrl());
      result.setPrice(sampleProduct.getPrice());
      result.setProductStatusCode("SUCCESS");
      result.setSuccess(true);

      // DB에 저장
      saveProductResult(executionId, result);

      log.info(
          "상품 크롤링 완료 - executionId: {}, productName: {}", executionId, result.getProductName());

    } catch (Exception e) {
      log.error("상품 크롤링 중 오류 발생 - executionId: {}", executionId, e);

      // 실패 시 설정
      result.setSuccess(false);
      result.setErrorMessage(e.getMessage());
      result.setProductStatusCode("FAILED");

      // 실패도 DB에 기록
      saveProductResult(executionId, result);

      throw new RuntimeException("상품 크롤링 실패", e);
    }

    return result;
  }

  /** 프로토타입용 샘플 상품 정보 생성 */
  private ProductInfo generateSampleProduct(String keyword) {
    // 키워드별 샘플 상품 매핑
    String productName = "제품명: 최상의[" + keyword + "] ";
    String sourceUrl = "https://ssadagu.kr/product/" + keyword.toLowerCase() + "_premium";
    int price = generateRandomPrice();

    return new ProductInfo(productName, sourceUrl, price);
  }

  /** 랜덤 가격 생성 (프로토타입용) */
  private int generateRandomPrice() {
    int basePrice = (int) (Math.random() * 500000) + 50000; // 5만원~55만원
    return basePrice;
  }

  /** 상품 크롤링 결과를 DB에 저장 */
  private void saveProductResult(int executionId, ProductCrawlingResult result) {
    try {
      testDomainMapper.insertProductData(
          executionId,
          result.getProductName(),
          result.getSourceUrl(),
          result.getPrice(),
          result.getProductStatusCode());

      log.debug(
          "상품 데이터 저장 완료 - executionId: {}, productName: {}", executionId, result.getProductName());

    } catch (Exception e) {
      log.error("상품 데이터 저장 실패 - executionId: {}", executionId, e);
      throw e;
    }
  }

  // 내부 클래스
  private static class ProductInfo {
    private String productName;
    private String sourceUrl;
    private int price;

    public ProductInfo(String productName, String sourceUrl, int price) {
      this.productName = productName;
      this.sourceUrl = sourceUrl;
      this.price = price;
    }

    // getters
    public String getProductName() {
      return productName;
    }

    public String getSourceUrl() {
      return sourceUrl;
    }

    public int getPrice() {
      return price;
    }
  }
}
