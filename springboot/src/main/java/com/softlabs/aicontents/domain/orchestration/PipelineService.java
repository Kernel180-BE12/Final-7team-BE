package com.softlabs.aicontents.domain.orchestration;

import com.softlabs.aicontents.domain.orchestration.vo.PipeStatusResponseVO;
import com.softlabs.aicontents.domain.scheduler.service.executor.AIContentExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.BlogPublishExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.KeywordExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.ProductCrawlingExecutor;
import com.softlabs.aicontents.domain.scheduler.vo.StepExecutionResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Component
public class PipelineService {

  // 실행 인터페이스들만 주입
  @Autowired private KeywordExecutor keywordExecutor;

  @Autowired private ProductCrawlingExecutor crawlingExecutor;

  @Autowired private AIContentExecutor aiExecutor;

  @Autowired private BlogPublishExecutor blogExecutor;

  public PipeStatusResponseVO executionPipline() {

    ///  동기 실행 테스트 코드
    System.out.println("자동 실행 프로그램 시작");
    int executionId = createNewExecution();

    try {
      System.out.println("executionId =" + executionId);

      // step01 - 키워드 추출
      StepExecutionResultVO step01 = keywordExecutor.execute(executionId);
      // keyWordStatusCode 확인
      String keyWordStatusCode = step01.getKeyWordStatusCode();
      if ("SUCCESS".equals(keyWordStatusCode)) {
        System.out.println("DB조회 결과) keyWordStatusCode = " + keyWordStatusCode);
        System.out.println("키워드 추출 성공 - 다음 단계 진행");

        // step02 - 상품정보 & URL 추출
        StepExecutionResultVO step02 = crawlingExecutor.execute(executionId);
        // productStatusCode 확인
        String productStatusCode = step02.getProductStatusCode();
        if ("SUCCESS".equals(productStatusCode)) {
          System.out.println("DB조회 결과) productStatusCode = " + productStatusCode);
          System.out.println(" 상품정보 & URL 추출 성공 - 다음 단계 진행");

          // step03 - LLM 생성
          StepExecutionResultVO step03 = aiExecutor.execute(executionId);
          // aIContentStatusCode 확인
          String aIContentStatusCode = step03.getAIContentStatusCode();
          if ("SUCCESS".equals(aIContentStatusCode)) {
            System.out.println("DB조회 결과) aIContentStatusCode = " + aIContentStatusCode);
            System.out.println(" LLM 생성 성공 - 다음 단계 진행");

            // step04 - 블로그 발행
            StepExecutionResultVO step04 = blogExecutor.execute(executionId);
            // publishStatusCode 확인
            String publishStatusCode = step04.getPublishStatusCode();
            if ("SUCCESS".equals(publishStatusCode)) {
              System.out.println("DB조회 결과) publishStatusCode = " + publishStatusCode);
              System.out.println("블로그 발행 성공");
              System.out.println("================");
              System.out.println("자동 실행 프로그램 종료");

              return new PipeStatusResponseVO();
            } else {
              throw new RuntimeException("블로그 발행 실패: " + publishStatusCode);
            }
          } else {
            throw new RuntimeException("LLM 생성 실패: " + aIContentStatusCode);
          }
        } else {
          throw new RuntimeException("상품 크롤링 실패: " + productStatusCode);
        }
      } else {
        throw new RuntimeException("키워드 추출 실패: " + keyWordStatusCode);
      }

    } catch (Exception e) {
      log.error("파이프라인 실행 실패: {}", e.getMessage());
      e.printStackTrace();
      return null;
    }

    //

  }

  private int createNewExecution() {

    return 0; /// 일단은 Long타입의 기본값.
    // todo: return 반환값으로,
    // PIPELINE_EXECUTIONS 테이블에서 executionId를 새로 생성하고,
    // 이것을 가져오는 메서드 구현
    // => 파이프라인이 새로 실행될 때마다 executionId를 생성
  }

  private void updateExecutionStatus(int executionId, String failed) {
    // todo: PIPELINE_EXECUTIONS에 상태 업데이트하는 코드 구현(SUCCESS, FAILED,PENDING 등등등)
  }
}
