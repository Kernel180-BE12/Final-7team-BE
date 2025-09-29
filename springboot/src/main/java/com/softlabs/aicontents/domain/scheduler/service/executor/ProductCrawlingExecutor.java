package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.LogMapper;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.Product;
import com.softlabs.aicontents.domain.testDomainService.ProductCrawlingService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class ProductCrawlingExecutor {

  @Autowired private ProductCrawlingService productCrawlingService;
  @Autowired private PipelineMapper pipelineMapper;
  @Autowired private LogMapper logMapper;

  public ProductCrawlingResult productCrawlingExecute(
      int executionId, KeywordResult keywordResult, StatusApiResponseDTO statusApiResponseDTO) {

    // 1. 메서드 실행 + 결과 DB저장
    productCrawlingService.productCrawlingExecute(executionId, keywordResult);

    // 2. 실행 결과를 DB 조회+ 객체 저장
    ProductCrawlingResult productCrawlingResult =
        pipelineMapper.selctproductCrawlingStatuscode(executionId);
    List<Product> productList = new ArrayList<>();
    boolean success = false;

    // 3. null 체크
    if (productCrawlingResult == null) {
      System.out.println("NullPointerException");
      logMapper.insertStep_02Faild(executionId);
      success = false;

      productCrawlingResult = new ProductCrawlingResult();
      productCrawlingResult.setExecutionId(executionId);
      productCrawlingResult.setSuccess(false);
    }

    // 4. 완료 판단 = (product_name, source_url, price) != null && productStatusCode == "SUCCESS"
    if (productCrawlingResult.getProductName() != null
        && productCrawlingResult.getSourceUrl() != null
        && productCrawlingResult.getPrice() != null
        && "SUCCESS".equals(productCrawlingResult.getProductStatusCode())) {
      logMapper.insertStep_02Success(executionId);
      success = true;
      productCrawlingResult.setSuccess(true);

      Product product = new Product();
      product.setName(productCrawlingResult.getProductName());
      product.setUrl(productCrawlingResult.getSourceUrl());
      product.setPrice(productCrawlingResult.getPrice());
      product.setPlatform(productCrawlingResult.getPlatform());
      product.setSelected(true);
      productList.add(product);

    } else {
      logMapper.insertStep_02Faild(executionId);
      success = false;
      productCrawlingResult.setSuccess(false);

      Product product = new Product();
      product.setName(productCrawlingResult.getProductName());
      product.setUrl(productCrawlingResult.getSourceUrl());
      product.setPrice(productCrawlingResult.getPrice());
      product.setPlatform(productCrawlingResult.getPlatform());
      product.setSelected(false);
      productList.add(product);
    }

    if (success) {
      // 최종 응답 객체에 매핑
      statusApiResponseDTO
          .getProgress()
          .getProductCrawling()
          .setStatus(productCrawlingResult.getProductStatusCode());
      statusApiResponseDTO
          .getProgress()
          .getProductCrawling()
          .setProgress(productCrawlingResult.getProgress());
      statusApiResponseDTO.getStage().setProducts(productList);
    }
    System.out.println("\n\nstatusApiResponseDTO =" + statusApiResponseDTO + "\n\n");

    if (!success) {
      statusApiResponseDTO
          .getProgress()
          .getProductCrawling()
          .setStatus(productCrawlingResult.getProductStatusCode());
      statusApiResponseDTO
          .getProgress()
          .getProductCrawling()
          .setProgress(productCrawlingResult.getProgress());
      statusApiResponseDTO.getStage().setProducts(productList);
    }

    return productCrawlingResult;
  }
}

//        try {
//            //키워드 수집 서비스 실행
//            log.info("트랜드 키워드 추출 메서스 시작");
//            productCrawlingService.extractproductCrawling(executionId);
//            // todo: 실제 싸다구 정보 수집 서비스의 추출 메서드
//
//            //DB 조회로 결과 확인 (30초 대기 적용)
//            String keyword = waitForResult(executionId, 30);
//
//            if (keyword != null) {
//                log.info("✅ 트렌드 키워드 추출 완료: {}", keyword);
//                return StepExecutionResultDTO.success(keyword);
//
//            } else {
//                return StepExecutionResultDTO.failure("트렌드 키워드 추출 시간 초과");
//            }
//
//        } catch (Exception e) {
//            log.error("트렌드 키워드 추출 실패", e);
//            return StepExecutionResultDTO.failure(e.getMessage());
//        }
//    }

//
//    ///  이런 시간 제한으로 결과 확인은 비추 - 언제 끝나는 지 명확히 알아야 함
//    private String waitForResult(Long executionId, int timeoutSeconds) {
//        for (int i = 0; i < timeoutSeconds; i++) {
//            String keyword = productCrawlingMapper.findproductCrawlingByExecutionId(executionId);
//            if (keyword != null) {
//                return keyword;
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                break;
//            }
//        }
//        return null;
//    }
// }
