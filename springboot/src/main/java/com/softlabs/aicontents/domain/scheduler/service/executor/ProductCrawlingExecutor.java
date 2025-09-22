package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.StepExecutionResultVO;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.domain.scheduler.interfacePipe.PipelineStepExecutor;
// import com.softlabs.aicontents.domain.testMapper.ProductCrawlingMapper;
import com.softlabs.aicontents.domain.testDomainService.ProductCrawlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class ProductCrawlingExecutor {

  @Autowired private ProductCrawlingService productCrawlingService;
  // todo: 실제 싸다구 정보 수집 서비스 클래스로 변경

  @Autowired
  private PipelineMapper pipelineMapper;

  public ProductCrawlingResult productCrawlingExecute(int executionId, KeywordResult keywordResult) {

    //1. 메서드 실행
    System.out.println("\n\nkeywordResult에 기반한 크롤링-상품 정보 수집 메서드 실행 - productCrawlingService(keywordResult)");

    productCrawlingService.productCrawlingExecute(executionId, keywordResult);
    System.out.println("\n\n 2단계 메서드 실행됐고, 결과를 DB에 저장했다.\n\n");

    //2. 실행 결과를 DB 조회+ 객체 저장
    ProductCrawlingResult productCrawlingResult = pipelineMapper.selctproductCrawlingStatuscode(executionId);

    //3.null 체크
    if (productCrawlingResult == null) {
      System.out.println("NullPointerException 감지");
      productCrawlingResult = new ProductCrawlingResult();
      productCrawlingResult.setSuccess(false);
      productCrawlingResult.setExecutionId(executionId);
    }

    //4. 완료 판단 =
    //  (product_name, source_url, price)!= null && productStatusCode = "SUCCEDSS"
    if(productCrawlingResult.getProductName() != null && productCrawlingResult.getSourceUrl()!= null &&
            productCrawlingResult.getPrice()!= null && "SUCCESS".equals(productCrawlingResult.getProductStatusCode())){
      productCrawlingResult.setSuccess(true);
    }else {
      productCrawlingResult.setSuccess(false);
    }
    System.out.println("여기 탔음" + productCrawlingResult);


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
