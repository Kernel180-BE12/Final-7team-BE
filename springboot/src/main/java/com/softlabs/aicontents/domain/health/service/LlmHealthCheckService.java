package com.softlabs.aicontents.domain.health.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LlmHealthCheckService {
    private final RestTemplate restTemplate; //외부 API 호출용 객체

    //RestTemplateBuilder를 주입받아 RestTemplate 생성
    public LlmHealthCheckService(RestTemplateBuilder builder){
        this.restTemplate=builder.build();
    }

    // FastAPI 서버 상태 확인 메서드
    public boolean isUp(){
        try{
            //FastAPI 헬스체크 URL
            String url="http://13.124.8.131/fastapi/health";

            //FastAPI 호출 -> 응답을 Map으로 받음
            Map<String, Object> response=restTemplate.getForObject(url,Map.class);

            //응답이 null이 아니고 status 값이 "ok" 일때 true 반환
            //"status":"ok"
            return response != null && "ok".equals(response.get("status"));
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
