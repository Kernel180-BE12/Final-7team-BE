package com.softlabs.aicontents.domain.orchestration;

import com.softlabs.aicontents.domain.orchestration.dto.PipeExecuteData;
import com.softlabs.aicontents.domain.orchestration.dto.PipeStatusExcIdReqDTO;
import com.softlabs.aicontents.domain.orchestration.mapper.LogMapper;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.BlogPublishResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.*;
import com.softlabs.aicontents.domain.scheduler.service.executor.AIContentExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.BlogPublishExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.KeywordExecutor;
import com.softlabs.aicontents.domain.scheduler.service.executor.ProductCrawlingExecutor;
// import com.softlabs.aicontents.domain.orchestration.refreshCache.CacheRefreshService;
import java.util.ArrayList;
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

  @Autowired private PipelineMapper pipelineMapper;

  @Autowired private LogMapper logMapper;

  public StatusApiResponseDTO executionPipline(PipeStatusExcIdReqDTO reqDTO) {

    // 1. 파이프라인 테이블의 ID(executionId) 생성

    int executionId = createNewExecution();
    reqDTO.setExecutionId(executionId);
    PipeExecuteData pipeExecuteData = new PipeExecuteData();

    StatusApiResponseDTO statusApiResponseDTO = new StatusApiResponseDTO();
    ProgressResult progressResult = new ProgressResult();
    progressResult.setKeywordExtraction(new KeywordExtraction());
    progressResult.setProductCrawling(new ProductCrawling());
    progressResult.setContentGeneration(new ContentGeneration());
    progressResult.setContentPublishing(new ContentPublishing());

    StageResults stageResults = new StageResults();
    stageResults.setKeywords(new ArrayList<>());
    stageResults.setProducts(new ArrayList<>());
    stageResults.setContent(new Content());
    stageResults.setPublishingStatus(new PublishingStatus());

    statusApiResponseDTO.setProgress(progressResult);
    statusApiResponseDTO.setStage(stageResults);
    statusApiResponseDTO.setLogs(new ArrayList<>());

    //
    statusApiResponseDTO.setExecutionId(executionId);
    // statusApiResponseDTO에는 taskId 필드가 없으므로 제거
    statusApiResponseDTO.setOverallStatus("PENDING");
    statusApiResponseDTO.setCurrentStage("START_PIPELINE");

    System.out.println("파이프라인 시작점 executionId=" + executionId);

    try {

      // STEP_00 워크플로우 시작
      logMapper.insertStep_00(executionId);

      statusApiResponseDTO.setOverallStatus("RUNNING");
      statusApiResponseDTO.setCurrentStage("START_PIPELINE");
      // return statusApiResponseDTO;

      // STEP_01 - 키워드 추출
      KeywordResult keywordResult =
          keywordExecutor.keywordExecute(executionId, statusApiResponseDTO);
      System.out.println("파이프라인 1단계 결과/  " + keywordResult);
      //  STEP_01 완료 판단
      if (!isStep01Completed(executionId)) {
        logMapper.insertStep_01Faild(executionId); // -> 파이프라인 실패 - 크롤링 실패
        System.out.println("1단계 실패 - 다음 단계로 진행");
        statusApiResponseDTO.setOverallStatus("RUNNING");
        statusApiResponseDTO.setCurrentStage("CRAWILING-KEYWORD");
        // keywordExtraction.setStatus("Faild"); // 변수 선언 필요
        // return statusApiResponseDTO;
      } else {
        logMapper.insertStep_01Success(executionId);
        System.out.println("1단계 완료 확인됨 - 다음 단계로 진행");
        statusApiResponseDTO.setOverallStatus("RUNNING");
        statusApiResponseDTO.setCurrentStage("CRAWILING-KEYWORD");
        // keywordExtraction.setStatus("COMPLETED"); // 변수 선언 필요
        //      return statusApiResponseDTO;
      }

      /// todo: 파이프라인 테이블에 상태 저장

      // STEP_02 - 상품정보 & URL 추출
      ProductCrawlingResult productCrawlingResultExecution =
          crawlingExecutor.productCrawlingExecute(executionId, keywordResult, statusApiResponseDTO);
      System.out.println("파이프라인 2단계 결과/  " + productCrawlingResultExecution);
      //  STEP_02 완료 판단
      if (!isStep02Completed(executionId)) {
        logMapper.insertStep_02Faild(executionId);
        throw new RuntimeException("2단계 실패: 상품 정보 추출이 완료되지 않았습니다");
      }
      logMapper.insertStep_02Success(executionId);
      System.out.println("2단계 완료 확인됨 - 다음 단계로 진행");

      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      // if (!step1.isSuccess()) {
      // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

      /// todo: executeApiResponseDTO 결과물 저장 메서드

      // STEP_03 - LLM 생성
      AIContentsResult aIContentsResultExecution =
          aiExecutor.aIContentsResultExecute(
              executionId, productCrawlingResultExecution, statusApiResponseDTO);
      System.out.println("파이프라인 3단계 결과/  " + aIContentsResultExecution);
      //  STEP_03 완료 판단
      if (!isStep03Completed(executionId)) {
        logMapper.insertStep_03Faild(executionId);
        throw new RuntimeException("3단계 실패: LLM 생성이 완료되지 않았습니다");
      }
      logMapper.insertStep_03Success(executionId);
      System.out.println("3단계 완료 확인됨 - 다음 단계로 진행");
      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      /// todo: executeApiResponseDTO 결과물 저장 메서드

      // STEP_04 - 블로그 발행
      BlogPublishResult blogPublishResultExecution =
          blogExecutor.blogPublishResultExecute(
              executionId, aIContentsResultExecution, statusApiResponseDTO);
      System.out.println("파이프라인 4단계 결과/  " + blogPublishResultExecution);
      //  STEP_04 완료 판단
      if (!isStep04Completed(executionId)) {
        logMapper.insertStep_04Faild(executionId);
        throw new RuntimeException("4단계 실패: 발행이 완료되지 않았습니다");
      }
      logMapper.insertStep_04Success(executionId);
      System.out.println("4단계 완료 확인됨 - 워크 플로우 종료");

      log.info("파이프라인 성공");

      // STEP_99 워크플로우 종료
      logMapper.insertStep_99(executionId);

      return statusApiResponseDTO;

    } catch (Exception e) {
      log.error("파이프라인 실행 실패: executionId={}, error={}", executionId, e.getMessage(), e);

      // statusApiResponseDTO가 NULL인 경우 생성
      if (statusApiResponseDTO == null) {
        statusApiResponseDTO = createSafeStatusResponse(executionId);
      }

      // Progress와 Stage가 NULL인 경우 초기화
      if (statusApiResponseDTO.getProgress() == null) {
        progressResult = new ProgressResult();
        progressResult.setKeywordExtraction(new KeywordExtraction());
        progressResult.setProductCrawling(new ProductCrawling());
        progressResult.setContentGeneration(new ContentGeneration());
        progressResult.setContentPublishing(new ContentPublishing());
        statusApiResponseDTO.setProgress(progressResult);
      } else {
        progressResult = statusApiResponseDTO.getProgress();
      }

      if (statusApiResponseDTO.getStage() == null) {
        stageResults = new StageResults();
        stageResults.setKeywords(new ArrayList<>());
        stageResults.setProducts(new ArrayList<>());
        stageResults.setContent(new Content());
        stageResults.setPublishingStatus(new PublishingStatus());
        statusApiResponseDTO.setStage(stageResults);
      } else {
        stageResults = statusApiResponseDTO.getStage();
      }

      if (statusApiResponseDTO.getLogs() == null) {
        statusApiResponseDTO.setLogs(new ArrayList<>());
      }

      statusApiResponseDTO.setExecutionId(executionId);
      statusApiResponseDTO.setOverallStatus("FAILED");
      statusApiResponseDTO.setCurrentStage("ERROR");
      statusApiResponseDTO.setCompletedAt(java.time.LocalDateTime.now().toString());

      // 모든 단계를 실패로 설정
      setAllStepsToFailed(statusApiResponseDTO.getProgress());

      // 에러 로그 추가
      Logs errorLog = new Logs();
      errorLog.setStage("ERROR");
      errorLog.setMessage("Pipeline execution failed: " + e.getMessage());
      errorLog.setTimestamp(java.time.LocalDateTime.now().toString());
      statusApiResponseDTO.getLogs().add(errorLog);

      try {
        logMapper.insertStep_00Faild(executionId);
      } catch (Exception logException) {
        log.error("로그 삽입 실패: {}", logException.getMessage());
      }

      return statusApiResponseDTO;
    }
  }

  //
  //  // @GetMapping("/pipeline/status/{executionId}")
  //  public StatusApiResponseDTO getStatusPipline(int executionId) {
  //
  //    // 파이프라인의 상태/결과 누적
  //    StatusApiResponseDTO statusApiResponseDTO = new StatusApiResponseDTO();
  //
  //    // 1. 실행정보
  //    statusApiResponseDTO.setExecutionId(executionId);
  //    statusApiResponseDTO.setOverallStatus("running");
  //    statusApiResponseDTO.setCurrentStage("product_crawling");
  //
  //    // 데이터 조회
  //    KeywordResult keywordResultStatus = pipelineMapper.selectKeywordStatuscode(executionId);
  //    ProductCrawlingResult productResultStatus =
  //        pipelineMapper.selctproductCrawlingStatuscode(executionId);
  //    AIContentsResult aiContentsResultStatus =
  // pipelineMapper.selectAiContentStatuscode(executionId);
  //    BlogPublishResult blogPublishResultStatus =
  // pipelineMapper.selectPublishStatuscode(executionId);
  //
  //    // 2. 각 단계별 진행 상황
  //    ProgressResult progressResult = new ProgressResult();
  //
  //    // 1단계 진행 상황 조회
  //    KeywordExtraction keywordExtraction = new KeywordExtraction();
  //    if (keywordResultStatus != null
  //        && "SUCCESS".equals(keywordResultStatus.getKeyWordStatusCode())) {
  //      if (keywordResultStatus.getKeyword() != null) {
  //        keywordExtraction.setStatus("completed");
  //        keywordExtraction.setProgress(100);
  //      } else {
  //        keywordExtraction.setStatus("running");
  //        keywordExtraction.setProgress(65);
  //      }
  //    } else if (keywordResultStatus == null
  //        || "FAILED".equals(keywordResultStatus.getKeyWordStatusCode())) {
  //      keywordExtraction.setStatus("failed");
  //      keywordExtraction.setProgress(0);
  //    }
  //    // 1단계 상태를 progressResult에 저장
  //    progressResult.setKeywordExtraction(keywordExtraction);
  //
  //    // 2단계 진행 상황 조회
  //    ProductCrawling productCrawling = new ProductCrawling();
  //    if (productResultStatus != null
  //        && "SUCCESS".equals(productResultStatus.getProductStatusCode())) {
  //      if (productResultStatus.getProductName() != null
  //          && productResultStatus.getSourceUrl() != null
  //          && productResultStatus.getPrice() != null) {
  //        productCrawling.setStatus("completed");
  //        productCrawling.setProgress(100);
  //      } else {
  //        productCrawling.setStatus("running");
  //        productCrawling.setProgress(65);
  //      }
  //
  //    } else if (productResultStatus == null
  //        || "FAILED".equals(productResultStatus.getProductStatusCode())) {
  //      productCrawling.setStatus("failed");
  //      productCrawling.setProgress(0);
  //    }
  //
  //    // 2단계 상태를 progressResult에 저장
  //    progressResult.setProductCrawling(productCrawling);
  //
  //    // 3단계 진행 상황 조회
  //    ContentGeneration contentGeneration = new ContentGeneration();
  //    if (aiContentsResultStatus != null
  //        && "SUCCESS".equals(aiContentsResultStatus.getAIContentStatusCode())) {
  //      if (aiContentsResultStatus.getTitle() != null
  //          && aiContentsResultStatus.getSummary() != null
  //          && aiContentsResultStatus.getHashtags() != null
  //          && aiContentsResultStatus.getContent() != null
  //          && aiContentsResultStatus.getSourceUrl() != null) {
  //        contentGeneration.setStatus("completed");
  //        contentGeneration.setProgress(100);
  //      } else {
  //        contentGeneration.setStatus("running");
  //        contentGeneration.setProgress(65);
  //      }
  //
  //    } else if (aiContentsResultStatus == null
  //        || "FAILED".equals(aiContentsResultStatus.getAIContentStatusCode())) {
  //      contentGeneration.setStatus("failed");
  //      contentGeneration.setProgress(0);
  //    }
  //    // 3단계 상태를 progressResult에 저장
  //    progressResult.setContentGeneration(contentGeneration);
  //
  //    // 4단계 진행 상황 조회
  //    ContentPublishing contentPublishing = new ContentPublishing();
  //    if (blogPublishResultStatus != null
  //        && "SUCCESS".equals(blogPublishResultStatus.getPublishStatusCode())) {
  //      if (blogPublishResultStatus.getBlogPlatform() != null
  //          && blogPublishResultStatus.getBlogPostId() != null
  //          && blogPublishResultStatus.getBlogUrl() != null) {
  //        contentPublishing.setStatus("completed");
  //        contentPublishing.setProgress(100);
  //      } else {
  //        contentPublishing.setStatus("running");
  //        contentPublishing.setProgress(65);
  //      }
  //
  //    } else if (blogPublishResultStatus == null
  //        || "FAILED".equals(blogPublishResultStatus.getPublishStatusCode())) {
  //      contentPublishing.setStatus("failed");
  //      contentPublishing.setProgress(0);
  //    }
  //
  //    // 4단계 상태를 progressResult에 저장
  //    progressResult.setContentPublishing(contentPublishing);
  //
  //    // 실행 상태를 응답 객체(StatusApiResponseDTO)에 저장
  //    statusApiResponseDTO.setProgress(progressResult);
  //    System.out.println("\n\n\n\n진행 상태가 statusApiResponseDTO에 저장 됐어?" + statusApiResponseDTO);
  //
  //    // 3. 단계별 결과 데이터
  //    StageResults stageResults = new StageResults();
  //
  //    //  KeywordResult → List<Keyword> 매핑
  //    List<Keyword> listKeywords = new ArrayList<>();
  //    if (keywordResultStatus != null && keywordResultStatus.getKeyword() != null) {
  //      Keyword keyword = new Keyword();
  //      keyword.setKeyword(keywordResultStatus.getKeyword());
  //      keyword.setSelected(true);
  //      keyword.setRelevanceScore(50);
  //      listKeywords.add(keyword);
  //    } else {
  //      listKeywords = new ArrayList<>();
  //    }
  //    stageResults.setKeywords(listKeywords);
  //
  //    //  ProductCrawlingResult → List<Product> 매핑
  //    List<Product> listProducts = new ArrayList<>();
  //    if (productResultStatus != null
  //        && productResultStatus.getProductName() != null
  //        && productResultStatus.getSourceUrl() != null
  //        && productResultStatus.getPrice() != null
  //        && productResultStatus.getPlatform() != null) {
  //      Product product = new Product();
  //      product.setProductId(productResultStatus.getSourceUrl());
  //      product.setName(productResultStatus.getProductName());
  //      product.setPrice(productResultStatus.getPrice());
  //      product.setPlatform(productResultStatus.getPlatform());
  //      listProducts.add(product);
  //    } else {
  //      listProducts = new ArrayList<>();
  //    }
  //
  //    stageResults.setProducts(listProducts);
  //
  //    //  AIContentsResult → Content 매핑
  //    Content content = new Content();
  //    if (aiContentsResultStatus != null
  //        && aiContentsResultStatus.getTitle() != null
  //        && aiContentsResultStatus.getSummary() != null
  //        && aiContentsResultStatus.getHashtags() != null
  //        && aiContentsResultStatus.getContent() != null) {
  //      content.setTitle(aiContentsResultStatus.getTitle());
  //      content.setContent(aiContentsResultStatus.getContent());
  //
  //      List<String> tags = new ArrayList<>();
  //      if (aiContentsResultStatus.getHashtags() != null) {
  //        String[] hashtags = aiContentsResultStatus.getHashtags().split(",");
  //        for (String tag : hashtags) {
  //          tags.add(tag.trim());
  //        }
  //      } else {
  //        tags = new ArrayList<>();
  //      }
  //      content.setTags(tags);
  //    } else {
  //      content = new Content();
  //    }
  //    stageResults.setContent(content);
  //
  //    //  - BlogPublishResult → PublishingStatus 매핑
  //    PublishingStatus publishingStatus = new PublishingStatus();
  //    if (blogPublishResultStatus != null
  //        && blogPublishResultStatus.getBlogPlatform() != null
  //        && blogPublishResultStatus.getBlogPostId() != null
  //        && blogPublishResultStatus.getBlogUrl() != null) {
  //      publishingStatus.setPlatform(blogPublishResultStatus.getBlogPlatform());
  //      publishingStatus.setStatus(blogPublishResultStatus.getPublishStatusCode());
  //      publishingStatus.setUrl(blogPublishResultStatus.getBlogUrl());
  //    } else {
  //      publishingStatus = new PublishingStatus();
  //    }
  //
  //    stageResults.setPublishingStatus(publishingStatus);
  //
  //    statusApiResponseDTO.setStage(stageResults);
  //    System.out.println("statusApiResponseDTO 반환 =" + statusApiResponseDTO);
  //
  //    // 4. 로그 정보
  //    // todo
  //
  //    return statusApiResponseDTO;
  //  }

  /** 메서드 정의 */
  public int createNewExecution() {

    try {
      // 1. 삽입
      pipelineMapper.insertNewExecutionId();
      // 2. 조회
      PipeExecuteData pipeExecuteData = pipelineMapper.selectNewExecutionId();

      // NULL 체크
      if (pipeExecuteData == null) {
        log.error("Failed to create new execution - PipeExecuteData is null");
        throw new RuntimeException("Failed to create new execution ID");
      }

      // 3. 객체 저장
      int executionId = pipeExecuteData.getExecutionId();

      if (executionId <= 0) {
        log.error("Invalid execution ID generated: {}", executionId);
        throw new RuntimeException("Invalid execution ID generated");
      }

      log.info("New execution ID created successfully: {}", executionId);
      return executionId;

    } catch (Exception e) {
      log.error("Failed to create new execution: {}", e.getMessage(), e);
      throw new RuntimeException("Database operation failed during execution ID creation", e);
    }
  }

  private void updateExecutionStatus(int executionId, String failed) {
    // todo: PIPELINE_EXECUTIONS에 상태 업데이트하는 코드 구현(SUCCESS, FAILED,PENDING 등등등)
  }

  // step01 완료 판단
  private boolean isStep01Completed(int executionId) {
    KeywordResult keywordResult = pipelineMapper.selectKeywordStatuscode(executionId);

    if (keywordResult == null) {
      return false;
    }

    if (keywordResult.getKeyword() != null
        && "SUCCESS".equals(keywordResult.getKeyWordStatusCode())) {
      return true;
    } else {
      return false;
    }
  }

  // step02 완료 판단
  private boolean isStep02Completed(int executionId) {
    ProductCrawlingResult productCrawlingResult =
        pipelineMapper.selctproductCrawlingStatuscode(executionId);

    if (productCrawlingResult == null) {
      return false;
    }

    if (productCrawlingResult.getProductName() != null
        && productCrawlingResult.getSourceUrl() != null
        && productCrawlingResult.getPrice() != null
        && "SUCCESS".equals(productCrawlingResult.getProductStatusCode())) {
      return true;
    } else {
      return false;
    }
  }

  // step03 완료 판단
  private boolean isStep03Completed(int executionId) {
    AIContentsResult aiContentsResult = pipelineMapper.selectAiContentStatuscode(executionId);

    if (aiContentsResult == null) {
      return false;
    }

    if (aiContentsResult.getTitle() != null
        && aiContentsResult.getSummary() != null
        && aiContentsResult.getHashtags() != null
        && aiContentsResult.getContent() != null
        && aiContentsResult.getSourceUrl() != null
        && "SUCCESS".equals(aiContentsResult.getAIContentStatusCode())) {
      return true;
    } else {
      return false;
    }
  }

  // step04 완료 판단
  private boolean isStep04Completed(int executionId) {
    BlogPublishResult blogPublishResult = pipelineMapper.selectPublishStatuscode(executionId);

    if (blogPublishResult == null) {
      return false;
    }

    if (blogPublishResult.getBlogPlatform() != null
        && blogPublishResult.getBlogPostId() != null
        && blogPublishResult.getBlogUrl() != null
        && "SUCCESS".equals(blogPublishResult.getPublishStatusCode())) {

      return true;
    } else {
      return false;
    }
  }

  /** NULL 안전한 StatusApiResponseDTO 생성 */
  private StatusApiResponseDTO createSafeStatusResponse(int executionId) {
    StatusApiResponseDTO safeResponse = new StatusApiResponseDTO();

    // 필수 정보 설정
    safeResponse.setExecutionId(executionId);
    safeResponse.setOverallStatus("FAILED");
    safeResponse.setCurrentStage("ERROR");
    safeResponse.setCompletedAt(java.time.LocalDateTime.now().toString());

    // Progress 객체 안전 초기화
    ProgressResult progressResult = new ProgressResult();
    progressResult.setKeywordExtraction(new KeywordExtraction());
    progressResult.setProductCrawling(new ProductCrawling());
    progressResult.setContentGeneration(new ContentGeneration());
    progressResult.setContentPublishing(new ContentPublishing());
    safeResponse.setProgress(progressResult);

    // Stage 객체 안전 초기화
    StageResults stageResults = new StageResults();
    stageResults.setKeywords(new ArrayList<>());
    stageResults.setProducts(new ArrayList<>());
    stageResults.setContent(new Content());
    stageResults.setPublishingStatus(new PublishingStatus());
    safeResponse.setStage(stageResults);

    // 로그 초기화
    safeResponse.setLogs(new ArrayList<>());

    return safeResponse;
  }

  /** 모든 단계를 실패 상태로 설정 */
  private void setAllStepsToFailed(ProgressResult progressResult) {
    if (progressResult != null) {
      if (progressResult.getKeywordExtraction() != null) {
        progressResult.getKeywordExtraction().setStatus("FAILED");
        progressResult.getKeywordExtraction().setProgress(0);
      }
      if (progressResult.getProductCrawling() != null) {
        progressResult.getProductCrawling().setStatus("FAILED");
        progressResult.getProductCrawling().setProgress(0);
      }
      if (progressResult.getContentGeneration() != null) {
        progressResult.getContentGeneration().setStatus("FAILED");
        progressResult.getContentGeneration().setProgress(0);
      }
      if (progressResult.getContentPublishing() != null) {
        progressResult.getContentPublishing().setStatus("FAILED");
        progressResult.getContentPublishing().setProgress(0);
      }
    }
  }
}
