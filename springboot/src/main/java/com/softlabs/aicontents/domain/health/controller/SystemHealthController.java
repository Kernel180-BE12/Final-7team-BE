package com.softlabs.aicontents.domain.health.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.health.dto.response.SystemHealthDTO;
import com.softlabs.aicontents.domain.health.service.SystemHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor //final 필드 생성자 자동 생성
public class SystemHealthController {
    private SystemHealthService systemHealthService;


    @GetMapping("/health")
    public ApiResponseDTO<SystemHealthDTO> getSystemHealth(){
        try{
            SystemHealthDTO dto=systemHealthService.getSystemHealth();
            return ApiResponseDTO.success(dto);
        }catch (Exception e){
            return ApiResponseDTO.error("조회 실패");
        }
    }
}
