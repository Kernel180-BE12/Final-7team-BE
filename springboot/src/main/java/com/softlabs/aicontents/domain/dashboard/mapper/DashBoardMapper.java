package com.softlabs.aicontents.domain.dashboard.mapper;

import com.softlabs.aicontents.domain.dashboard.vo.DashboardResponseVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashBoardMapper {
  List<DashboardResponseVO> selectMenu();
}
