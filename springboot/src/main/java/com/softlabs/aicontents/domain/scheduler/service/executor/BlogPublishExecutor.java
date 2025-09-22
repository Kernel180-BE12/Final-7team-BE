package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.BlogPublishResult;
// import com.softlabs.aicontents.domain.testMapper.BlogPublishMapper;
import com.softlabs.aicontents.domain.testDomainService.BlogPublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class BlogPublishExecutor {
  @Autowired private BlogPublishService blogPublishService;
  // todo: 실제 발행 클래스로 변경

  @Autowired private PipelineMapper pipelineMapper;

  public BlogPublishResult blogPublishResultExecute(
      int executionId, AIContentsResult aIContentsResult) {

    // 1. 메서드 실행
    System.out.println("\n\n발행 메서드 실행 - blogPublishService(aIContentsResult)\n\n");

    blogPublishService.extractBlogPublish(executionId, aIContentsResult);
    System.out.println("\n\n 4단계 메서드 실행됐고, 결과를 DB에 저장했다.\n\n");

    // 2. 실행결과를 DB 조회 +객체 저장
    BlogPublishResult blogPublishResult = pipelineMapper.selectPublishStatuscode(executionId);

    // 3. null 체크
    if (blogPublishResult == null) {
      System.out.println("NullPointerException 감지");
      blogPublishResult = new BlogPublishResult();
      blogPublishResult.setSuccess(false);
      blogPublishResult.setExecutionId(executionId);
    }

    // 4. 완료 판단
    if (blogPublishResult.getBlogPlatform() != null
        && blogPublishResult.getBlogPostId() != null
        && blogPublishResult.getBlogUrl() != null
        && "SUCCESS".equals(blogPublishResult.getPublishStatusCode())) {

      blogPublishResult.setSuccess(true);
    } else {
      blogPublishResult.setSuccess(false);
    }

    System.out.println("여기 탔음" + blogPublishResult);
    return blogPublishResult;
  }
}

//        try {
//            //키워드 수집 서비스 실행
//            log.info("LLM생성 메서스 시작");
//            blogPublishService.extractBlogPublish(executionId);
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
//            String keyword = blogPublishMapper.findBlogPublishByExecutionId(executionId);
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
