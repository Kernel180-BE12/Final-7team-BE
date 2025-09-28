package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.LogMapper;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
// import com.softlabs.aicontents.domain.testMapper.AIContentMapper;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.Content;
import com.softlabs.aicontents.domain.testDomainService.AIContentService;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class AIContentExecutor {

  @Autowired private AIContentService aiContentService;
  @Autowired private PipelineMapper pipelineMapper;
  @Autowired private LogMapper logMapper;

  public AIContentsResult aIContentsResultExecute(
      int executionId, ProductCrawlingResult productCrawlingResult, StatusApiResponseDTO statusApiResponseDTO) {

    // 1. 메서드 실행
    System.out.println("LLM 생성 메서드 실행 - aiContentService(productCrawlingResult)");

    aiContentService.aIContentsResultExecute(executionId, productCrawlingResult);
    System.out.println("\n\n 3단계 메서드 실행됐고, 결과를 DB에 저장했다.\n\n");

    // 2. 실행 결과를 DB 조회 + 객체에 저장
    AIContentsResult aiContentsResult = pipelineMapper.selectAiContentStatuscode(executionId);

    // 3. null 체크
    if (aiContentsResult == null) {
      System.out.println("NullPointerException 감지");
      logMapper.insertStep_03Faild(executionId);
      aiContentsResult = new AIContentsResult();
      aiContentsResult.setSuccess(false);
      aiContentsResult.setExecutionId(executionId);
    }

    Content content = new Content();
    boolean success = false;

    // 4. 완료 판단
    if (aiContentsResult.getTitle() != null
        && aiContentsResult.getSummary() != null
        && aiContentsResult.getHashtags() != null
        && aiContentsResult.getContent() != null
        && aiContentsResult.getSourceUrl() != null
        && "SUCCESS".equals(aiContentsResult.getAIContentStatusCode())) {

      logMapper.insertStep_03Success(executionId);
      success = true;
      aiContentsResult.setSuccess(true);

      content.setTitle(aiContentsResult.getTitle());
      content.setSummary(aiContentsResult.getSummary());
      content.setContent(aiContentsResult.getContent());
      content.setTags(Arrays.asList(aiContentsResult.getHashtags().split(",")));

    } else {
      logMapper.insertStep_03Faild(executionId);
      success = false;
      aiContentsResult.setSuccess(false);

      content.setTitle(aiContentsResult.getTitle());
      content.setSummary(aiContentsResult.getSummary());
      content.setContent(aiContentsResult.getContent());
      if (aiContentsResult.getHashtags() != null) {
        content.setTags(Arrays.asList(aiContentsResult.getHashtags().split(",")));
      }
    }

    if(success) {
      statusApiResponseDTO.getProgress().getContentGeneration().setStatus(aiContentsResult.getAIContentStatusCode());
      statusApiResponseDTO.getProgress().getContentGeneration().setProgress(aiContentsResult.getProgress());
      statusApiResponseDTO.getStage().setContent(content);
    }
    System.out.println("\n\nstatusApiResponseDTO ="+statusApiResponseDTO+"\n\n");

    if (!success) {
      statusApiResponseDTO.getProgress().getContentGeneration().setStatus(aiContentsResult.getAIContentStatusCode());
      statusApiResponseDTO.getProgress().getContentGeneration().setProgress(aiContentsResult.getProgress());
      statusApiResponseDTO.getStage().setContent(content);
    }

    return aiContentsResult;
  }
}

//        try {
//            //키워드 수집 서비스 실행
//            log.info("LLM생성 메서스 시작");
//            aiContentService.extractAiContent(executionId);
//            // todo: 실제 키워드 수집 서비스 의 추출 메서드
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
//    private String waitForResult(Long executionId, int timeoutSeconds) {
//        for (int i = 0; i < timeoutSeconds; i++) {
//            String keyword = aiContentMapper.findAicontentByExecutionId(executionId);
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
