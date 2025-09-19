package com.softlabs.aicontents.domain.orchestration;

import com.softlabs.aicontents.domain.orchestration.vo.StepExecutionResultVO;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.BlogPublishResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.scheduler.dto.PipeResultDataDTO;
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



  public PipeResultDataDTO executionPipline() {

    //1. 파이프라인 테이블의 ID(executionId) 생성
    int executionId = createNewExecution();

    // 파이프라인의 상태/결과 누적
    PipeResultDataDTO pipeResultDataDTO = new PipeResultDataDTO();

    try {

        // step01 - 키워드 추출
        KeywordResult keywordResult01 = keywordExecutor.keywordExecute(executionId);
        System.out.println("파이프라인 1단계 결과/  " +keywordResult01);
        // todo : if 추출 실패 시 3회 재시도 및 예외처리
            // if (!step1.isSuccess()) {
            // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

        /// todo: PipeResultDataDTO에 결과물 저장 메서드


        // step02 - 상품정보 & URL 추출
      ProductCrawlingResult productCrawlingResult01 = crawlingExecutor.productCrawlingExecute(executionId,keywordResult01);
      System.out.println("파이프라인 2단계 결과/  " +productCrawlingResult01);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      // if (!step1.isSuccess()) {
      // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

      /// todo: PipeResultDataDTO에 결과물 저장 메서드


      // step03 - LLM 생성
      AIContentsResult aIContentsResult01 = aiExecutor.aIContentsResultExecute(executionId,productCrawlingResult01);
      System.out.println("파이프라인 3단계 결과/  " +aIContentsResult01);

      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      /// todo: PipeResultDataDTO에 결과물 저장 메서드


      // step04 - 블로그 발행
      BlogPublishResult blogPublishResult01 = blogExecutor.blogPublishResultExecute(executionId,aIContentsResult01);
      System.out.println("파이프라인 4단계 결과/  " +blogPublishResult01);
      //          // todo : if 추출 실패 시 3회 재시도 및 예외처리

        log.info("파이프라인 성공");

        return pipeResultDataDTO;

    } catch (Exception e) {
      log.error("파이프라인 실행 실패:{}", e.getMessage());
      updateExecutionStatus(executionId, "FAILED");
    }
      return null;
  }



  private int createNewExecution() {

    return 0;
    // todo: return 반환값으로,
    // PIPELINE_EXECUTIONS 테이블에서 executionId를 새로 생성하고,
    // 이것을 가져오는 메서드 구현
    // => 파이프라인이 새로 실행될 때마다 executionId를 생성
  }

  private void updateExecutionStatus(int executionId, String failed) {
    // todo: PIPELINE_EXECUTIONS에 상태 업데이트하는 코드 구현(SUCCESS, FAILED,PENDING 등등등)
  }
}
