package com.softlabs.aicontents.domain.dashboard.mapper;

import com.softlabs.aicontents.domain.dashboard.vo.DashboardResponseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashBoardMapper {
    List<DashboardResponseVO> selectMenu();
}
