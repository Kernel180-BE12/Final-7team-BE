package com.softlabs.aicontents.scheduler.service;

import com.softlabs.aicontents.domain.testService.ProductCrawlingService;
import com.softlabs.aicontents.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.scheduler.interfacePipe.PipelineStepExecutor;
// import com.softlabs.aicontents.domain.testMapper.ProductCrawlingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class ProductCrawlingExecutor implements PipelineStepExecutor {

  @Autowired private ProductCrawlingService productCrawlingService;

  // todo: 실제 싸다구 정보 수집 서비스 클래스로 변경

  //    @Autowired
  //    private ProductCrawlingMapper productCrawlingMapper;
  //    // todo: 실제 싸다구 정보 수집 매퍼 인터페이스로 변경

  @Override
  public StepExecutionResultDTO execute(Long executionId) {

    /// test : 파이프라인 동작 테스트
    System.out.println("싸다구 정보 수집 메서드 호출/ 실행");
    delayWithDots(3);

    /// todo : 테스트용 RDS 조회 쿼리
    System.out.println("싸다구 정보 수집 결과 DB에서 쿼리 조회");
    delayWithDots(3);
    System.out.println("싸다구 정보 수집 결과 DB 완료 확인 로직 실행");
    delayWithDots(3);
    System.out.println("싸다구 정보 수집 상태 판단 -> 완료(success)");
    System.out.println("싸다구 정보 수집 상태 판단 -> 실패(failure)-> 재시도/예외처리");
    delayWithDots(3);
    System.out.println("[스케줄러]가 [싸다구 정보 수집] -> [LLM] (요청)객체 전달");
    delayWithDots(3);
    return null;
    /// todo : 반환 값으로 이전 기능이 요구하는 파라메터를 반환하기.
  }

  /// 테스트용 딜레이 메서드
  private void delayWithDots(int seconds) {
    try {
      for (int i = 0; i < seconds; i++) {
        Thread.sleep(200); // 1초마다
        System.out.print(".");
      }
      System.out.println(); // 줄바꿈
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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
