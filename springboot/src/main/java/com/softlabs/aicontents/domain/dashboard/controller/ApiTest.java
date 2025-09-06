package com.softlabs.aicontents.domain.dashboard.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.dashboard.dto.DashBoardReqDTO;
import com.softlabs.aicontents.domain.dashboard.dto.DashBoardResDTO;
import com.softlabs.aicontents.domain.dashboard.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/dashboard")
public class ApiTest {

    @Autowired
    DashBoardService dashBoardService;

    // 1. 연결 테스트
    @GetMapping("/connectTest")
    public String test(DashBoardReqDTO reqDTO) {

        String role = reqDTO.getRole();
        System.out.println("role = " + role);
        if(!role.equals("admin") || role == null) {
            return "권한이 없습니다.";
        }

        String userName = reqDTO.getUserName();
        return userName + " Hello:)";
    }

    // 2. 대시보드 데이터 조회
    @GetMapping("/navMenu")
    public ApiResponseDTO<List<DashBoardResDTO>> getNavMenu() {
        try {
            List<DashBoardResDTO> menuList = dashBoardService.getNavMenu();
            return ApiResponseDTO.success(menuList);
        } catch (Exception e) {
            return ApiResponseDTO.error("메뉴 조회 중 오류가 발생했습니다.");
        }
    }
}
