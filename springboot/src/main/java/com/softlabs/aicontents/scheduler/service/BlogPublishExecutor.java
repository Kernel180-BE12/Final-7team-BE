package com.softlabs.aicontents.scheduler.service;

import com.softlabs.aicontents.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.scheduler.interfacePipe.PipelineStepExecutor;
import com.softlabs.aicontents.domain.testMapper.BlogPublishMapper;
import com.softlabs.aicontents.domain.testService.BlogPublishService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
@Slf4j
@Service
public class BlogPublishExecutor implements PipelineStepExecutor {
    @Autowired
    private BlogPublishService blogPublishService;
    // todo: 실제 LLM생성 클래스로 변경

    @Autowired
    private BlogPublishMapper blogPublishMapper;
    // todo: 실제 LLM생성 매퍼 인터페이스로 변경


    @Override
    public StepExecutionResultDTO execute(Long executionId) {


        try {
            //키워드 수집 서비스 실행
            log.info("LLM생성 메서스 시작");
            blogPublishService.extractBlogPublish(executionId);
            // todo: 실제 키워드 수집 서비스 의 추출 메서드

            //DB 조회로 결과 확인 (30초 대기 적용)
            String keyword = waitForResult(executionId, 30);

            if (keyword != null) {
                log.info("✅ 트렌드 키워드 추출 완료: {}", keyword);
                return StepExecutionResultDTO.success(keyword);

            } else {
                return StepExecutionResultDTO.failure("트렌드 키워드 추출 시간 초과");
            }

        } catch (Exception e) {
            log.error("트렌드 키워드 추출 실패", e);
            return StepExecutionResultDTO.failure(e.getMessage());
        }
    }
    private String waitForResult(Long executionId, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds; i++) {
            String keyword = blogPublishMapper.findBlogPublishByExecutionId(executionId);
            if (keyword != null) {
                return keyword;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }
}
