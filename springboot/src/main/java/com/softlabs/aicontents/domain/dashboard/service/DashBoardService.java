package com.softlabs.aicontents.domain.dashboard.service;

import com.softlabs.aicontents.domain.dashboard.dto.DashBoardResDTO;
import com.softlabs.aicontents.domain.dashboard.mapper.DashBoardMapper;
import com.softlabs.aicontents.domain.dashboard.vo.DashboardResponseVO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashBoardService {

  @Autowired DashBoardMapper dashBoardMapper;

  public List<DashBoardResDTO> getNavMenu() {

    List<DashboardResponseVO> res = dashBoardMapper.selectMenu();
    List<DashBoardResDTO> dashBoardResDTO =
        res.stream()
            .map(DashBoardResDTO::new) // 생성자 참조 사용
            .collect(Collectors.toList());
    return dashBoardResDTO;
  }
}
