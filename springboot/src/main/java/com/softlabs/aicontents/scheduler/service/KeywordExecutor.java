package com.softlabs.aicontents.scheduler.service;


import com.softlabs.aicontents.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.scheduler.interfacePipe.PipelineStepExecutor;
import com.softlabs.aicontents.domain.testMapper.KeywordMapper;
import com.softlabs.aicontents.domain.testService.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
@Slf4j
@Service
public class KeywordExecutor implements PipelineStepExecutor {

    @Autowired
    private KeywordService keywordService; // 실제 기능 서비스

    @Autowired
    private KeywordMapper keywordMapper;  // DB 조회용

    @Override
    public StepExecutionResultDTO execute(Long executionId) {

        try {
            // 🎬 1. 서비스 실행
            log.info("🚀 트렌드 키워드 추출 실행 시작");
            keywordService.extractTrendKeyword(executionId);

            // 🔍 2. DB 조회로 결과 확인 (최대 30초 대기)
            String keyword = waitForResult(executionId, 30);

            if (keyword != null) {
                log.info("✅ 트렌드 키워드 추출 완료: {}", keyword);
                return StepExecutionResultDTO.success(keyword);
            } else {
                return StepExecutionResultDTO.failure("트렌드 키워드 추출 시간 초과");
            }

        } catch (Exception e) {
            log.error("❌ 트렌드 키워드 추출 실패", e);
            return StepExecutionResultDTO.failure(e.getMessage());
        }
    }

    private String waitForResult(Long executionId, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds; i++) {
            String keyword = keywordMapper.findKeywordByExecutionId(executionId);
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
