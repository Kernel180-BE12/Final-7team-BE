package com.softlabs.aicontents.domain.health.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.health.dto.response.SystemHealthDTO;
import com.softlabs.aicontents.domain.health.service.SystemHealthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor
public class SystemHealthController {
    private final SystemHealthService systemHealthService;

    @GetMapping("/health")
    @Operation(summary = "시스템 상태 조회 API", description = "시스템 상태 조회를 위한 API입니다.")
    public ApiResponseDTO<SystemHealthDTO> getSystemHealth(){
        try{
            SystemHealthDTO dto=systemHealthService.getSystemHealth();
            return ApiResponseDTO.success(dto);
        }catch (Exception e){
            log.error("에러",e);
            return ApiResponseDTO.error("조회 실패");
        }
    }
}
