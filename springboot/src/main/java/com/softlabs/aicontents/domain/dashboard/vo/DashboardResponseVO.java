package com.softlabs.aicontents.domain.dashboard.vo;

public record DashboardResponseVO(
        Integer id,           // int → Integer
        String label,
        String path,
        Integer orderseq,     // int → Integer
        String roleRequired,
        Boolean isActive      // boolean → Boolean
) {}