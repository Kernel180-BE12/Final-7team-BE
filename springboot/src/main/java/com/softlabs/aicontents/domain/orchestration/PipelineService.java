package com.softlabs.aicontents.domain.orchestration;

import com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.domain.scheduler.service.executor.AIContentExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.BlogPublishExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.KeywordExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.ProductCrawlingExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Component
public class PipelineService {

  // 🎯 실행 인터페이스들만 주입
  @Autowired private KeywordExecutor keywordExecutor;

  @Autowired private ProductCrawlingExecutor crawlingExecutor;

  @Autowired private AIContentExecutor aiExecutor;

  @Autowired private BlogPublishExecutor blogExecutor;

  public void executionPipline() {
    Long executionId = createNewExecution();
    // todo : executionId = (동일한 파이프라인인지 구분하는 용도)
    // DB 에서 PIPELINE_EXECUTIONS 테이블의 execution_id

    try { ///  파이프라인 전체 try-catch
      // 각 단계를 순차적으로 실행 (실행과 검증이 포함되어 있음)

      // step01 - 키워드 추출
      StepExecutionResultDTO step01 = keywordExecutor.execute(executionId);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      // 예시 :
      // if (!step1.isSuccess()) {
      // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

      // step02 - 상품정보 & URL 추출
      StepExecutionResultDTO step02 = crawlingExecutor.execute(executionId);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리

      // step03 - LLM 생성
      StepExecutionResultDTO step03 = aiExecutor.execute(executionId);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리

      // step04 - 블로그 발행
      StepExecutionResultDTO step04 = blogExecutor.execute(executionId);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리

      log.info("파이프라인 성공");

//      return

    } catch (Exception e) {
      log.error("파이프라인 실행 실패:{}", e.getMessage());
      updateExecutionStatus(executionId, "FAILED");
    }
  }

  private Long createNewExecution() {

    return 0L; /// 일단은 Long타입의 기본값.
    // todo: return 반환값으로,
    // PIPELINE_EXECUTIONS 테이블에서 executionId를 새로 생성하고,
    // 이것을 가져오는 메서드 구현
    // => 파이프라인이 새로 실행될 때마다 executionId를 생성
  }

  private void updateExecutionStatus(Long executionId, String failed) {
    // todo: PIPELINE_EXECUTIONS에 상태 업데이트하는 코드 구현(SUCCESS, FAILED,PENDING 등등등)
  }
}
