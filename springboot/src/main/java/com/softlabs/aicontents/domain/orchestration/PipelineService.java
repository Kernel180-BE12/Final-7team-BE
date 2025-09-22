package com.softlabs.aicontents.domain.orchestration;

import com.softlabs.aicontents.domain.orchestration.dto.ExecuteApiResponseDTO;
import com.softlabs.aicontents.domain.orchestration.dto.PipeExecuteData;
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
import java.util.ArrayList;
import java.util.List;
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

  // @PostMapping("/execute")
  public ExecuteApiResponseDTO executionPipline() {

    // 1. 파이프라인 테이블의 ID(executionId) 생성
    int executionId = createNewExecution();
    PipeExecuteData pipeExecuteData = new PipeExecuteData();
    ExecuteApiResponseDTO executeApiResponseDTO = new ExecuteApiResponseDTO();

    System.out.println("executionId=" + executionId);
    // 2. PipeExecuteData 채우기

    pipeExecuteData.setExecutionId(executionId);
    System.out.println(pipeExecuteData.getExecutionId()+"생성된 ID는 여기 있다.");
    pipeExecuteData.setStatus("started");
    pipeExecuteData.setEstimatedDuration("약 35분");
    pipeExecuteData.setStages(List.of("keyword_extraction", "product_crawling", "content_generation", "content_publishing"));
    executeApiResponseDTO.setData(pipeExecuteData);

    // 3. ExecuteApiResponseDTO 채우기
    executeApiResponseDTO.setSuccess(true);
    executeApiResponseDTO.setMessage("파이프라인 실행이 시작되었습니다");

    System.out.println("파이프라인 시작점" + executionId);

    try {

      // step01 - 키워드 추출
      KeywordResult keywordResultExecution = keywordExecutor.keywordExecute(executionId);
      System.out.println("파이프라인 1단계 결과/  " + keywordResultExecution);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      // if (!step1.isSuccess()) {
      // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

      /// todo: executeApiResponseDTO 결과물 저장 메서드
      /// todo: 파이프라인 테이블에 상태 저장

      // step02 - 상품정보 & URL 추출
      ProductCrawlingResult productCrawlingResultExecution =
          crawlingExecutor.productCrawlingExecute(executionId, keywordResultExecution);
      System.out.println("파이프라인 2단계 결과/  " + productCrawlingResultExecution);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      // if (!step1.isSuccess()) {
      // throw new RuntimeException("1단계 실패: " + step1.getErrorMessage());

      /// todo: executeApiResponseDTO 결과물 저장 메서드

      // step03 - LLM 생성
      AIContentsResult aIContentsResultExecution =
          aiExecutor.aIContentsResultExecute(executionId, productCrawlingResultExecution);
      System.out.println("파이프라인 3단계 결과/  " + aIContentsResultExecution);

      // todo : if 추출 실패 시 3회 재시도 및 예외처리
      /// todo: executeApiResponseDTO 결과물 저장 메서드

      // step04 - 블로그 발행
      BlogPublishResult blogPublishResultExecution =
          blogExecutor.blogPublishResultExecute(executionId, aIContentsResultExecution);
      System.out.println("파이프라인 4단계 결과/  " + blogPublishResultExecution);
      // todo : if 추출 실패 시 3회 재시도 및 예외처리

      log.info("파이프라인 성공");

      return executeApiResponseDTO;

    } catch (Exception e) {
      log.error("파이프라인 실행 실패:{}", e.getMessage());
      updateExecutionStatus(executionId, "FAILED");
    }
    return null;
  }

  // @GetMapping("/pipeline/status/{executionId}")
  public StatusApiResponseDTO getStatusPipline(int executionId) {

    // 파이프라인의 상태/결과 누적
    StatusApiResponseDTO statusApiResponseDTO = new StatusApiResponseDTO();

    // 1. 실행정보
    statusApiResponseDTO.setExecutionId(executionId);
    statusApiResponseDTO.setOverallStatus("running");
    statusApiResponseDTO.setCurrentStage("product_crawling");

    // 기존 쿼리로 데이터 조회
    KeywordResult keywordResultStatus = pipelineMapper.selectKeywordStatuscode(executionId);
    ProductCrawlingResult productResultStatus =
        pipelineMapper.selctproductCrawlingStatuscode(executionId);
    AIContentsResult aiContentsResultStatus = pipelineMapper.selectAiContentStatuscode(executionId);
    BlogPublishResult blogPublishResultStatus = pipelineMapper.selectPublishStatuscode(executionId);

    // 2. 각 단계별 진행 상황
    ProgressResult progressResult = new ProgressResult();

    // 1단계 진행 상황 조회
    KeywordExtraction keywordExtraction = new KeywordExtraction();
    if (keywordResultStatus != null
        && "SUCCESS".equals(keywordResultStatus.getKeyWordStatusCode())) {
      if (keywordResultStatus.getKeyword() != null) {
        keywordExtraction.setStatus("completed");
        keywordExtraction.setProgress(100);
      } else {
        keywordExtraction.setStatus("running");
        keywordExtraction.setProgress(65);
      }
    } else if (keywordResultStatus == null
        || "FAILED".equals(keywordResultStatus.getKeyWordStatusCode())) {
      keywordExtraction.setStatus("failed");
      keywordExtraction.setProgress(0);
    }
    // 1단계 상태를 progressResult에 저장
    progressResult.setKeywordExtraction(keywordExtraction);

    // 2단계 진행 상황 조회
    ProductCrawling productCrawling = new ProductCrawling();
    if (productResultStatus != null
        && "SUCCESS".equals(productResultStatus.getProductStatusCode())) {
      if (productResultStatus.getProductName() != null
          && productResultStatus.getSourceUrl() != null
          && productResultStatus.getPrice() != null) {
        productCrawling.setStatus("completed");
        productCrawling.setProgress(100);
      } else {
        productCrawling.setStatus("running");
        productCrawling.setProgress(65);
      }

    } else if (productResultStatus == null
        || "FAILED".equals(productResultStatus.getProductStatusCode())) {
      productCrawling.setStatus("failed");
      productCrawling.setProgress(0);
    }

    // 2단계 상태를 progressResult에 저장
    progressResult.setProductCrawling(productCrawling);

    // 3단계 진행 상황 조회
    ContentGeneration contentGeneration = new ContentGeneration();
    if (aiContentsResultStatus != null
        && "SUCCESS".equals(aiContentsResultStatus.getAIContentStatusCode())) {
      if (aiContentsResultStatus.getTitle() != null
          && aiContentsResultStatus.getSummary() != null
          && aiContentsResultStatus.getHashtags() != null
          && aiContentsResultStatus.getContent() != null
          && aiContentsResultStatus.getSourceUrl() != null) {
        contentGeneration.setStatus("completed");
        contentGeneration.setProgress(100);
      } else {
        contentGeneration.setStatus("running");
        contentGeneration.setProgress(65);
      }

    } else if (aiContentsResultStatus == null
        || "FAILED".equals(aiContentsResultStatus.getAIContentStatusCode())) {
      contentGeneration.setStatus("failed");
      contentGeneration.setProgress(0);
    }
    // 3단계 상태를 progressResult에 저장
    progressResult.setContentGeneration(contentGeneration);

    // 4단계 진행 상황 조회
    ContentPublishing contentPublishing = new ContentPublishing();
    if (blogPublishResultStatus != null
        && "SUCCESS".equals(blogPublishResultStatus.getPublishStatusCode())) {
      if (blogPublishResultStatus.getBlogPlatform() != null
          && blogPublishResultStatus.getBlogPostId() != null
          && blogPublishResultStatus.getBlogUrl() != null) {
        contentPublishing.setStatus("completed");
        contentPublishing.setProgress(100);
      } else {
        contentPublishing.setStatus("running");
        contentPublishing.setProgress(65);
      }

    } else if (blogPublishResultStatus == null
        || "FAILED".equals(blogPublishResultStatus.getPublishStatusCode())) {
      contentPublishing.setStatus("failed");
      contentPublishing.setProgress(0);
    }

    // 4단계 상태를 progressResult에 저장
    progressResult.setContentPublishing(contentPublishing);

    // 실행 상태를 응답 객체(StatusApiResponseDTO)에 저장
    statusApiResponseDTO.setProgress(progressResult);
    System.out.println("\n\n\n\n진행 상태가 statusApiResponseDTO에 저장 됐어?" + statusApiResponseDTO);

    // 3. 단계별 결과 데이터
    StageResults stageResults = new StageResults();

    //  - KeywordResult → List<Keyword> 매핑
    List<Keyword> listKeywords = new ArrayList<>();
    if (keywordResultStatus != null && keywordResultStatus.getKeyword() != null) {
      Keyword keyword = new Keyword();
      keyword.setKeyword(keywordResultStatus.getKeyword());
      keyword.setSelected(true);
      keyword.setRelevanceScore(50);
      listKeywords.add(keyword);
    } else {
      listKeywords = new ArrayList<>();
    }
    stageResults.setKeywords(listKeywords);

    //  - ProductCrawlingResult → List<Product> 매핑
    List<Product> listProducts = new ArrayList<>();
    if (productResultStatus != null
        && productResultStatus.getProductName() != null
        && productResultStatus.getSourceUrl() != null
        && productResultStatus.getPrice() != null
        && productResultStatus.getPlatform() != null) {
      Product product = new Product();
      product.setProductId(productResultStatus.getSourceUrl());
      product.setName(productResultStatus.getProductName());
      product.setPrice(productResultStatus.getPrice());
      product.setPlatform(productResultStatus.getPlatform());
      listProducts.add(product);
    } else {
      listProducts = new ArrayList<>();
    }

    stageResults.setProducts(listProducts);

    //  - AIContentsResult → Content 매핑
    Content content = new Content();
    if (aiContentsResultStatus != null
        && aiContentsResultStatus.getTitle() != null
        && aiContentsResultStatus.getSummary() != null
        && aiContentsResultStatus.getHashtags() != null
        && aiContentsResultStatus.getContent() != null) {
      content.setTitle(aiContentsResultStatus.getTitle());
      content.setContent(aiContentsResultStatus.getContent());

      List<String> tags = new ArrayList<>();
      if (aiContentsResultStatus.getHashtags() != null) {
        String[] hashtags = aiContentsResultStatus.getHashtags().split(",");
        for (String tag : hashtags) {
          tags.add(tag.trim());
        }
      } else {
        tags = new ArrayList<>();
      }
      content.setTags(tags);
    } else {
      content = new Content();
    }
    stageResults.setContent(content);

    //  - BlogPublishResult → PublishingStatus 매핑
    PublishingStatus publishingStatus = new PublishingStatus();
    if (blogPublishResultStatus != null
        && blogPublishResultStatus.getBlogPlatform() != null
        && blogPublishResultStatus.getBlogPostId() != null
        && blogPublishResultStatus.getBlogUrl() != null) {
      publishingStatus.setPlatform(blogPublishResultStatus.getBlogPlatform());
      publishingStatus.setStatus(blogPublishResultStatus.getPublishStatusCode());
      publishingStatus.setUrl(blogPublishResultStatus.getBlogUrl());
    } else {
      publishingStatus = new PublishingStatus();
    }

    stageResults.setPublishingStatus(publishingStatus);

    statusApiResponseDTO.setStageResults(stageResults);
    System.out.println("statusApiResponseDTO 반환 =" + statusApiResponseDTO);

    // 4. 로그 정보

    return statusApiResponseDTO;
  }

  public int createNewExecution() {

    // 1. 삽입
    pipelineMapper.insertNewExecutionId();
    // 2. 조회
    PipeExecuteData pipeExecuteData = pipelineMapper.selectNewExecutionId();
    // 3. 객체 저장
    int executionId = pipeExecuteData.getExecutionId();

    return executionId;
  }

  private void updateExecutionStatus(int executionId, String failed) {
    // todo: PIPELINE_EXECUTIONS에 상태 업데이트하는 코드 구현(SUCCESS, FAILED,PENDING 등등등)
  }
}
