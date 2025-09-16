package com.softlabs.aicontents.domain.dashboard.dto;

import com.softlabs.aicontents.domain.dashboard.vo.DashboardResponseVO;

public record DashBoardResDTO(
    int id, String label, String path, int orderseq, String roleRequired, boolean isActive) {
  // 단일 VO를 받는 생성자
  public DashBoardResDTO(DashboardResponseVO vo) {
    this(vo.id(), vo.label(), vo.path(), vo.orderseq(), vo.roleRequired(), vo.isActive());
  }
}
