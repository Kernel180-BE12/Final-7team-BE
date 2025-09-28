package com.softlabs.aicontents.domain.scheduler.service.executor;

import com.softlabs.aicontents.domain.orchestration.mapper.LogMapper;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
// import com.softlabs.aicontents.domain.testMapper.KeywordMapper;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.Keyword;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.KeywordExtraction;
import com.softlabs.aicontents.domain.testDomainService.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Service
public class KeywordExecutor {

  @Autowired private KeywordService keywordService;
  @Autowired private PipelineMapper pipelineMapper;
  @Autowired private LogMapper logMapper;

  public KeywordResult keywordExecute(int executionId, StatusApiResponseDTO statusApiResponseDTO) {

    // 1. 메서드 실행 + 결과 DB저장
    keywordService.collectKeywordAndSave(executionId);

    // 2. 실행 결과를 DB 조회+ 객체 저장
    KeywordResult keywordResult = pipelineMapper.selectKeywordStatuscode(executionId);
    List<Keyword> keywordList = new ArrayList<>();
    boolean success = false;


      // 3. null 체크
      if (keywordResult == null) {
        System.out.println("NullPointerException");
        logMapper.insertStep_01Faild(executionId);
        success = false;

        keywordResult = new KeywordResult();
        keywordResult.setExecutionId(executionId);
        keywordResult.setSelected(false);

      }

      // 4. 완료 판단 = keyword !=null, keyWordStatusCode =="SUCCESS"
      if (keywordResult.getKeyword() != null
              && "SUCCESS".equals(keywordResult.getKeyWordStatusCode())) {
        logMapper.insertStep_01Success(executionId);
        success = true;
        keywordResult.setSelected(true);
        Keyword keyword = new Keyword();
        keyword.setKeyword(keywordResult.getKeyword());
        keyword.setSelected(keywordResult.isSelected());
        keyword.setRelevanceScore(keywordResult.getRelevanceScore());
        keywordList.add(keyword);

      } else {
        logMapper.insertStep_01Faild(executionId);
        success = false;
        keywordResult.setSelected(false);
        Keyword keyword = new Keyword();
        keyword.setKeyword(keywordResult.getKeyword());
        keyword.setSelected(keywordResult.isSelected());
        keyword.setRelevanceScore(keywordResult.getRelevanceScore());
        keywordList.add(keyword);

      }

      if(success) {

        // 최종 응답 객체에 매핑 (StatusApiResponseDTO는 progress, stage 필드 사용)
        statusApiResponseDTO.getProgress().getKeywordExtraction().setStatus(keywordResult.getKeyWordStatusCode());
        statusApiResponseDTO.getProgress().getKeywordExtraction().setProgress(keywordResult.getProgress());
        statusApiResponseDTO.getStage().setKeywords(keywordList);
      }
      System.out.println("\n\nstatusApiResponseDTO ="+statusApiResponseDTO+"\n\n");

      if (!success) {
        statusApiResponseDTO.getProgress().getKeywordExtraction().setStatus(keywordResult.getKeyWordStatusCode());
        statusApiResponseDTO.getProgress().getKeywordExtraction().setProgress(keywordResult.getProgress());
        statusApiResponseDTO.getStage().setKeywords(keywordList);
      }


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
