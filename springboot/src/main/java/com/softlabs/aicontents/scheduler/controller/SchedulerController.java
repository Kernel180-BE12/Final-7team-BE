package com.softlabs.aicontents.scheduler.controller;

import com.softlabs.aicontents.scheduler.service.ScheduledTaskService;

import org.springframework.web.bind.annotation.*;

@RestController
// REST API를 처리하는 컨트롤러이다.
@RequestMapping("/api")
//이 클래스의 모든 URL은 /api로 시작한다
@CrossOrigin(origins = "*")
//모든 도메인(="*")에서 이 API 호출 허용

public class SchedulerController {

    private final ScheduledTaskService scheduledTaskService;

    public SchedulerController(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }
    //scheduledTaskService 타입의 변수 선언

    //더미 데이터 제거 -> 실제 DB 조회
    @GetMapping("/tasks")  //스케줄 목록을 조회
    //GET 방식으로 /api/test URL 요청이 오면 이 메서드 실행
    public String testConnection(){
        //테스트 메서드 선언
        int count = scheduledTaskService.getTaskCount();
        //service의 getTaskCount() 메서드 호출해서 결과를 count 변수에 저장

        return "RDS 연결 성공  TEST_SCHEDULED_TASKS 테이블에" + count + "개 조회됨";

    }
}
