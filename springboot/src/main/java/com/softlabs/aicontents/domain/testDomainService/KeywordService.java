package com.softlabs.aicontents.domain.testDomainService;

import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.testDomain.TestDomainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KeywordService {

  @Autowired
  private TestDomainMapper testDomainMapper;

  /**
   * 프로토타입용 키워드 수집 서비스
   * executionId를 받아서 샘플 키워드를 DB에 저장
   */
  public void collectKeywordAndSave(int executionId) {
    try {
      log.info("키워드 수집 시작 - executionId: {}", executionId);

      // 프로토타입용 샘플 키워드 (실제로는 구글 트렌드 API 등에서 수집)
      String sampleKeyword = generateSampleKeyword();

      // DB에 저장
      saveKeywordResult(executionId, sampleKeyword, "SUCCESS");

      log.info("키워드 수집 완료 - executionId: {}, keyword: {}", executionId, sampleKeyword);

    } catch (Exception e) {
      log.error("키워드 수집 중 오류 발생 - executionId: {}", executionId, e);
      // 실패 시에도 DB에 기록
      saveKeywordResult(executionId, null, "FAILED");
      throw new RuntimeException("키워드 수집 실패", e);
    }
  }

  /**
   * 프로토타입용 샘플 키워드 생성
   */
  private String generateSampleKeyword() {
    String[] sampleKeywords = {
            "에어컨", "선풍기", "아이스크림", "수박", "비타민",
            "운동화", "백팩", "노트북", "스마트폰", "이어폰"
    };

    // 랜덤하게 키워드 선택
    int randomIndex = (int) (Math.random() * sampleKeywords.length);
    return sampleKeywords[randomIndex];
  }

  /**
   * 키워드 결과를 DB에 저장
   */
  private void saveKeywordResult(int executionId, String keyword, String statusCode) {
    try {
      testDomainMapper.insertKeywordData(executionId, keyword, statusCode);
      log.debug("키워드 데이터 저장 완료 - executionId: {}, keyword: {}, status: {}",
              executionId, keyword, statusCode);
    } catch (Exception e) {
      log.error("키워드 데이터 저장 실패 - executionId: {}", executionId, e);
      throw e;
    }
  }
}