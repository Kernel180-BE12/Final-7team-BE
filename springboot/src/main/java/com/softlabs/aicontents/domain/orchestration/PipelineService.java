package com.softlabs.aicontents.domain.orchestration;

import com.softlabs.aicontents.domain.orchestration.dto.ExecuteApiResponseDTO;
import com.softlabs.aicontents.domain.orchestration.dto.PipeExecuteData;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.BlogPublishResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.scheduler.service.executor.AIContentExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.BlogPublishExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.KeywordExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.ProductCrawlingExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Component

public class PipelineService {

    // 실행 인터페이스들만 주입
    @Autowired
    private KeywordExecutor keywordExecutor;

    @Autowired
    private ProductCrawlingExecutor crawlingExecutor;

    @Autowired
    private AIContentExecutor aiExecutor;

    @Autowired
    private BlogPublishExecutor blogExecutor;

    @Autowired private PipelineMapper pipelineMapper;


// @PostMapping("/execute")
    public ExecuteApiResponseDTO executionPipline(){

        //1. 파이프라인 테이블의 ID(executionId) 생성
        int executionId = createNewExecution();

        //2. PipeExecuteData 채우기
        PipeExecuteData pipeExecuteData = new PipeExecuteData();
        pipeExecuteData.setExecutionId(executionId);
        pipeExecuteData.setStatus("started");
        pipeExecuteData.setEstimatedDuration("약 45분");
        pipeExecuteData.setStages(List.of("키워드 추출","상품 크롤링","컨텐츠 생성","컨텐츠 배포"));

        //3. ExecuteApiResponseDTO 채우기
        ExecuteApiResponseDTO executeApiResponseDTO = new ExecuteApiResponseDTO();
        executeApiResponseDTO.setSuccess(true);
        executeApiResponseDTO.setMessage("파이프라인 실행이 시작되었습니다");


        System.out.println("파이프라인 시작점"+executionId);

        try {

        // step01 - 키워드 추출
            KeywordResult keywordResult01 = keywordExecutor.keywordExecute(executionId);
            System.out.println("파이프라인 1단계 결과/  " + keywordResult01);
            // todo : if 추출 실패 시 3회 재시도 및 예외처리
            // if (!step1.isSuccess()) {
            // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

            /// todo: PipeResultDataDTO에 결과물 저장 메서드
            /// todo: 파이프라인 테이블에 상태 저장

        // step02 - 상품정보 & URL 추출
            ProductCrawlingResult productCrawlingResult01 = crawlingExecutor.productCrawlingExecute(executionId, keywordResult01);
            System.out.println("파이프라인 2단계 결과/  " + productCrawlingResult01);
            // todo : if 추출 실패 시 3회 재시도 및 예외처리
            // if (!step1.isSuccess()) {
            // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

            /// todo: PipeResultDataDTO에 결과물 저장 메서드

        // step03 - LLM 생성
            AIContentsResult aIContentsResult01 = aiExecutor.aIContentsResultExecute(executionId, productCrawlingResult01);
            System.out.println("파이프라인 3단계 결과/  " + aIContentsResult01);

            // todo : if 추출 실패 시 3회 재시도 및 예외처리
            /// todo: PipeResultDataDTO에 결과물 저장 메서드

        // step04 - 블로그 발행
            BlogPublishResult blogPublishResult01 = blogExecutor.blogPublishResultExecute(executionId, aIContentsResult01);
            System.out.println("파이프라인 4단계 결과/  " + blogPublishResult01);
            //          // todo : if 추출 실패 시 3회 재시도 및 예외처리

            log.info("파이프라인 성공");


            return executeApiResponseDTO;


        } catch (Exception e) {
            log.error("파이프라인 실행 실패:{}", e.getMessage());
            updateExecutionStatus(executionId, "FAILED");
        }
        return null;
    }



// @GetMapping("/pipeline/status/{executionId}")
    public StatusApiResponseDTO getStatusPipline( int executionId) {

        // 파이프라인의 상태/결과 누적
        StatusApiResponseDTO statusApiResponseDTO = new StatusApiResponseDTO();



        //1. 실행정보
        statusApiResponseDTO.setExecutionId(executionId);
        statusApiResponseDTO.setOverallStatus("running");
        statusApiResponseDTO.setCurrentStage("product_crawling");

        //기존 쿼리로 데이터 조회
        KeywordResult keywordResult = pipelineMapper.selectKeywordStatuscode(executionId);
        ProductCrawlingResult productResult = pipelineMapper.selctproductCrawlingStatuscode(executionId);


        //2. 각 단계별 진행 상황

        //3. 단계별 결과 데이터

        //4. 로그 정보

        return statusApiResponseDTO;
    }

    public int createNewExecution() {

        // 1. 삽입
        pipelineMapper.insertNewExecutionId();
        //2. 조회
        PipeExecuteData pipeExecuteData = pipelineMapper.selectNewExecutionId();
        //3. 객체 저장
        int executionId = pipeExecuteData.getExecutionId();

        return executionId;
    }

    private void updateExecutionStatus(int executionId, String failed) {
        // todo: PIPELINE_EXECUTIONS에 상태 업데이트하는 코드 구현(SUCCESS, FAILED,PENDING 등등등)
    }

}
