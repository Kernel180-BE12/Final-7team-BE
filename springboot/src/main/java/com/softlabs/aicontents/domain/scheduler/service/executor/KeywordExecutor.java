package com.softlabs.aicontents.domain.scheduler.service.executor;

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
public class KeywordExecutor implements PipelineStepExecutor {

  @Autowired private KeywordService keywordService;

  /// todo :  실제 키워드 수집 기능 서비스

  //    @Autowired
  //    private KeywordMapper keywordMapper;  // DB 조회용

  @Override
  public StepExecutionResultDTO execute(Long executionId) {

    /// test : 파이프라인 동작 테스트
    System.out.println("키워드 수집 메서드 호출/ 실행");
    delayWithDots(3);

    /// todo : 테스트용 RDS 조회 쿼리
    System.out.println("키워드 수집 결과 DB에서 쿼리 조회");
    delayWithDots(3);
    System.out.println("키워드 수집 결과 DB 완료 확인 로직 실행");
    delayWithDots(3);
    System.out.println("키워드 수집 수집 상태 판단 -> 완료(success)");
    System.out.println("키워드 수집 수집 상태 판단 -> 실패(failure)-> 재시도/예외처리");
    delayWithDots(3);
    System.out.println("[스케줄러]가 [키워드 수집] -> [싸다구 정보 수집] (요청)객체 전달");
    delayWithDots(3);
    return null;
    /// todo : 반환 값으로 이전 기능이 요구하는 파라메터를 반환하기.
  }

  /// 테스트용 딜레이 메서드
  private void delayWithDots(int seconds) {
    try {
      for (int i = 0; i < seconds; i++) {
        Thread.sleep(200); // 1초마다
        System.out.print(".");
      }
      System.out.println(); // 줄바꿈
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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
