package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponseDTO;
import com.softlabs.aicontents.domain.monitoring.mapper.UnifiedLogMapper;
import com.softlabs.aicontents.domain.monitoring.vo.response.UnifiedLogResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
//final 키워드가 붙은 필드만을 포함하는 생성자를 자동으로 만들어줌

public class MonitoringServiceImpl implements MonitoringService{

    private final UnifiedLogMapper unifiedLogMapper;
    //@RequiredArgsConstructor가 위 필드를 위한 생성자를 자동으로 생성
    //스프링의 의존성 주입(DI) 기능이 이 생성자를 발견하여 스프링 컨테이너에 등록된 UnifiedLogMapper타입의 Bean(객체)를 자동으로 주입

    @Override
    public MonitoringStatsResponseDTO getStats(){
        //1)DB에서 로그 최근 3건을 VO 리스트 형태로 조회
        List<UnifiedLogResponseVO> logVOs=unifiedLogMapper.findRecentLogs();
//        System.out.println("rows" + logVOs.size());

        //2) VO -> DTO 매핑(변환)
        //Java Stream API를 사용하여 각 vo 객체를 LogEntry DTO 객체로 변환
        List<MonitoringStatsResponseDTO.LogEntry> logs=logVOs.stream()
                .map(vo->new MonitoringStatsResponseDTO.LogEntry(
                        vo.logMessage(),
                        vo.logDetail(),
                        vo.statusCode()
                )).toList();

        //3) 성공 횟수, 실패 횟수, 성공률 통계
        //mapper를 통해 각 통계 쿼리를 실행, 반환된 숫자 값들을 가져옴
        int successCount= unifiedLogMapper.successCount();
        int failedCount= unifiedLogMapper.failedCount();
        float allRows= unifiedLogMapper.allRows();

//        float successRate= (successCount/allRows)*100;
        float successRate= (allRows > 0) ? (successCount/allRows)*100 : 0;
        //성공률 =(성공횟수/총시도횟수(모든행))*100
        //0으로 나누는 경우를 방지하는 로직 추가

        //계산된 통계 값들로 Stats DTO 객체를 생성
        var stats=new MonitoringStatsResponseDTO.Stats(successCount,failedCount,successRate);

        //4)최종 응답 DTO 조립 후 반환
        // 앞서 만등 통계(Stats)와 로그 목록(logs)을 합쳐 MonitoringStatsResponseDTO 객체 생성
        return new MonitoringStatsResponseDTO(stats,logs);
    }
}
