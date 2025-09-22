package com.softlabs.aicontents.domain.testDomain;

import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.testDomainService.AIContentService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestDomainMapper {

    void insertKeywordData(@Param("executionId") int executionId,
                           @Param("keyword") String keyword,
                           @Param("statusCode") String statusCode);

    void insertProductData(
            @Param("executionId") int executionId,
            @Param("productName") String productName,
            @Param("sourceUrl") String sourceUrl,
            @Param("price") int price,
            @Param("statusCode") String statusCode
    );

    // 상품 데이터 저장
    void insertProductData(
            @Param("executionId") Integer executionId,
            @Param("taskId") Integer taskId,
            @Param("productName") String productName,
            @Param("sourceUrl") String sourceUrl,
            @Param("price") Long price,
            @Param("statusCode") String statusCode
    );

    // executionId로 상품 데이터 조회
    AIContentsResult selectProductDataByExecutionId(@Param("executionId") Integer executionId);

    // AI 콘텐츠 데이터 저장
    void insertAIContentResult(
            @Param("executionId") Integer executionId,
            @Param("sourceUrl") String sourceUrl,
            @Param("title") String title,
            @Param("content") String content,
            @Param("summary") String summary,
            @Param("hashtags") String hashtags,
            @Param("statusCode") String statusCode
    );


    // AI 콘텐츠 데이터 조회
    AIContentsResult selectAIContentDataByExecutionId(@Param("executionId") Integer executionId);

    // 블로그 발행 결과 저장
    void insertBlogPublishResult(
            @Param("executionId") Integer executionId,
            @Param("blogPlatform") String blogPlatform,
            @Param("blogPostId") String blogPostId,
            @Param("blogUrl") String blogUrl,
            @Param("statusCode") String statusCode
    );


}
