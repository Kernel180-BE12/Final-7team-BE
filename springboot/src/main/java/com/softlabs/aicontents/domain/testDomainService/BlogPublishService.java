package com.softlabs.aicontents.domain.testDomainService;

import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.BlogPublishResult;
import com.softlabs.aicontents.domain.testDomain.TestDomainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class BlogPublishService {

  @Autowired private TestDomainMapper testDomainMapper;

  public BlogPublishResult extractBlogPublish(int executionId, AIContentsResult aIContentsResult) {

    BlogPublishResult result = new BlogPublishResult();
    result.setExecutionId(executionId);
    result.setStepCode("STEP04");

    try {
      log.info("블로그 발행 시작 - executionId: {}", executionId);

      // DB에서 AI 콘텐츠 정보 조회
      AIContentsResult aiContentData =
          testDomainMapper.selectAIContentDataByExecutionId(executionId);
      if (aiContentData == null) {
        throw new RuntimeException("executionId " + executionId + "에 해당하는 AI 콘텐츠 정보를 찾을 수 없습니다.");
      }

      // 프로토타입 블로그 발행 (하드코딩)
      String blogUrl = "https://prototype-blog.com/post/" + executionId;
      String blogPostId = "POST_" + executionId;
      String blogPlatform = "naver";

      result.setBlogUrl(blogUrl);
      result.setBlogPostId(blogPostId);
      result.setBlogPlatform(blogPlatform);
      result.setPublishStatusCode("SUCCESS");
      result.setSuccess(true);
      result.setResultData("블로그 발행 완료: " + blogUrl);

      // DB에 저장
      testDomainMapper.insertBlogPublishResult(
          executionId, blogPlatform, blogPostId, blogUrl, result.getPublishStatusCode());

      log.info("블로그 발행 완료 - executionId: {}, blogUrl: {}", executionId, blogUrl);

    } catch (Exception e) {
      log.error("블로그 발행 중 오류 발생 - executionId: {}", executionId, e);

      result.setSuccess(false);
      result.setErrorMessage(e.getMessage());
      result.setPublishStatusCode("FAILED");
      result.setResultData("블로그 발행 실패");

      throw new RuntimeException("블로그 발행 실패", e);
    }

    return result;
  }
}
