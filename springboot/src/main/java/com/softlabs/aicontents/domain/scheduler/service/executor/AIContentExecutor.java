package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.StepExecutionResultVO;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.domain.scheduler.interfacePipe.PipelineStepExecutor;
// import com.softlabs.aicontents.domain.testMapper.AIContentMapper;
import com.softlabs.aicontents.domain.testDomainService.AIContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class AIContentExecutor {

  @Autowired
  private AIContentService aiContentService;

  // todo: 실제 LLM생성 클래스로 변경

  @Autowired
  private PipelineMapper pipelineMapper;

  public AIContentsResult aIContentsResultExecute(int executionId, ProductCrawlingResult productCrawlingResult) {

    //1. 메서드 실행
    System.out.println("LLM 생성 메서드 실행 - aiContentService(productCrawlingResult)");

    //2. 실행 결과를 DB 조회 + 객체에 저장
    AIContentsResult aiContentsResult = pipelineMapper.selectAiContentStatuscode();

    //3. null 체크
    if (aiContentsResult == null) {
      System.out.println("NullPointerException 감지");
      aiContentsResult = new AIContentsResult();
      aiContentsResult.setSuccess(false);
      aiContentsResult.setExecutionId(executionId);
    }

    //4. 완료 판단
    if (aiContentsResult.getTitle() != null && aiContentsResult.getSummary() != null &&
            aiContentsResult.getHashtags() != null && aiContentsResult.getContent() != null &&
            aiContentsResult.getSourceUrl() != null && "SUCCESS".equals(aiContentsResult.getAIContentStatusCode())) {

      aiContentsResult.setSuccess(true);
    } else {
      aiContentsResult.setSuccess(false);
    }


    System.out.println("여기 탔음" + aiContentsResult);

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
