package com.softlabs.aicontents.domain.monitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.domain.monitoring.dto.response.ActivityEntry;
import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsSummaryDTO;
import com.softlabs.aicontents.domain.monitoring.mapper.MonitoringStatsMapper;
import com.softlabs.aicontents.domain.monitoring.vo.response.MonitoringStatsResponseVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// final 키워드가 붙은 필드만을 포함하는 생성자를 자동으로 만들어줌
public class MonitoringStatsServiceImpl implements MonitoringStatsService {
  // @RequiresArgsConstructor 가 위 필드를 위한 생성자르라 자동으로 생성
  private final MonitoringStatsMapper monitoringStatsMapper;

  // 스프링의 의존성 주입(DI) 기능이 이 생성자를 발견하여 스프링 컨테이너에 등록된 MonitoringStatsMapper타입의 Bean(객체)를 자동으로 주입

  @Override
  public MonitoringStatsSummaryDTO getStats() {
    // 1)DB에서 최근 작업의 발행 상태를 VO 리스트 형태로 조회
    List<MonitoringStatsResponseVO> logVOs = monitoringStatsMapper.findRecentLogs();

    // 2) VO -> DTO 매핑(변환)
    ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱을 위한 Jackson ObjectMapper 생성

    // DB에서 조회한 VO 리스트를 스트림으로 순회하며 DTO(ActivityEntry) 리스트로 뱐환
    List<ActivityEntry> activities =
        logVOs.stream()
            .map(
                vo -> {
                  // 1.DB에서 가져온 Log_detail 컬럼(JSON 형태 문자열)
                  String rawJson = vo.logDetail(); // log_detail 컬럼이 JSON일 경우

                  // 2.기본 값 초기화
                  String timestamp = "";
                  String description = "";
                  String type = vo.statusCode().toLowerCase(); // 예: "FAILED"

                  try {
                    // 3. log_detail을 JSON으로 파싱
                    JsonNode node = objectMapper.readTree(rawJson);

                    // 4.timestamp 추출 우선순위: finished_at > started_at
                    if (node.has("finished_at")) {
                      timestamp = node.get("finished_at").asText();
                    } else if (node.has("started_at")) {
                      timestamp = node.get("started_at").asText();
                    }

                    // 5.단계 정보가 있다면 description 구성
                    if (node.has("completed_steps") && node.has("total_steps")) {
                      description =
                          String.format(
                              "총 %d단계 중 %d단계 완료",
                              node.get("total_steps").asInt(), node.get("completed_steps").asInt());
                    }

                    // type 정제
                    if ("failed".equalsIgnoreCase(type)) {
                      type = "failure";
                    }

                  } catch (Exception e) {
                    // JSON 파싱 실패시 fallback
                    // fallback: DB의 created_at 값을 timestamp로 사용
                    timestamp = vo.createdAt().toString();
                  }

                  return new ActivityEntry(
                      vo.logId(), // log_id
                      vo.logMessage(), // 로그 메시지 title에 해당
                      description, // 전처리된 설명
                      type, // 상태
                      timestamp // 시간정보
                      );
                })
            .toList();

    // 3) 성공 횟수, 실패 횟수, 성공률 통계
    // mapper를 통해 각 통계 쿼리를 실행, 반환된 숫자 값들을 가져옴
    int successCount = monitoringStatsMapper.successCount();
    int failureCount = monitoringStatsMapper.failedCount();
    float allRows = monitoringStatsMapper.allRows();
    // 성공률 = (성공횟수/총시도횟수(모든행))*100
    // 0으로 나누는 경우를 방지하는 로직 추가

    int totalExecutions = monitoringStatsMapper.totalExecutions(); // 총 스케줄 개수
    int activeExecutions = monitoringStatsMapper.activeExecutions(); // 활성화 스케줄 개수

    float successRate = (allRows > 0) ? (successCount / allRows) * 100 : 0;

    // 계산된 통계 값들로 Stats DTO 객체를 생성
    //    var stats = new MonitoringStatsResponseDTO.Stats(successCount, failedCount, successRate);

    // 4)최종 응답 DTO 조립 후 반환
    // 앞서 만든 통계(Stats)와 로그 목록(logs)을 합쳐 MonitoringStatsResponseDTO 객체 생성
    return new MonitoringStatsSummaryDTO(
        successCount, failureCount, successRate, totalExecutions, activeExecutions, activities);
  }
}
