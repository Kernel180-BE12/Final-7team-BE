package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.orchestration.vo.StepExecutionResultVO;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;
import com.softlabs.aicontents.domain.scheduler.interfacePipe.PipelineStepExecutor;
// import com.softlabs.aicontents.domain.testMapper.KeywordMapper;
import com.softlabs.aicontents.domain.testDomainService.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
@Service
public class KeywordExecutor {

  @Autowired private KeywordService keywordService;
  /// todo :  실제 키워드 수집 기능 서비스

  @Autowired private PipelineMapper pipelineMapper;

  public KeywordResult keywordExecute(int executionId) {

    //1. 메서드 실행
        System.out.println("executionId를 받아서, 크롤링-트랜드 키워드 수집 메서드 실행 - keywordService");

    //2. 실행 결과를 DB 조회+ 객체 저장
        KeywordResult keywordResult = pipelineMapper.selectKeywordStatuscode();
         keywordResult.setExecutionId(executionId);

    //3. null 체크
    if (keywordResult == null) {
        System.out.println("NullPointerException 감지");
        keywordResult = new KeywordResult();
        keywordResult.setSuccess(false);
    }

    //4. 완료 판단 = keyword !=null, keyWordStatusCode =="SUCCESS"
    if (keywordResult.getKeyword() != null && "SUCCESS".equals(keywordResult.getKeyWordStatusCode())) {
        keywordResult.setSuccess(true);
    } else {
        keywordResult.setSuccess(false);
    }

    System.out.println("여기 탔음" + keywordResult);

        return keywordResult;

  }
}

        /// Todo : 하기 기능 구현 및 구체화
//        try {
//            // 🎬 1. 서비스 실행
//            log.info("🚀 트렌드 키워드 추출 실행 시작");
//            keywordService.extractTrendKeyword(executionId);
//
//            // 🔍 2. DB 조회로 결과 확인 (최대 30초 대기)
//            String keyword = waitForResult(executionId, 30);
//
//            if (keyword != null) {
//                log.info("✅ 트렌드 키워드 추출 완료: {}", keyword);
//                return StepExecutionResultDTO.success(keyword);
//            } else {
//                return StepExecutionResultDTO.failure("트렌드 키워드 추출 시간 초과");
//            }
//
//        } catch (Exception e) {
//            log.error("❌ 트렌드 키워드 추출 실패", e);
//            return StepExecutionResultDTO.failure(e.getMessage());
//        }
//    }
//
//    private String waitForResult(Long executionId, int timeoutSeconds) {
//        for (int i = 0; i < timeoutSeconds; i++) {
//            String keyword = keywordMapper.findKeywordByExecutionId(executionId);
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
