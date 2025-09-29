package com.softlabs.aicontents.domain.orchestration.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper {

  void insertStep_00(int executionId);

  void insertStep_00Faild(int executionId);

  void insertStep_01Success(int executionId);

  void insertStep_01Faild(int executionId);

  void insertStep_02Success(int executionId);

  void insertStep_02Faild(int executionId);

  void insertStep_03Success(int executionId);

  void insertStep_03Faild(int executionId);

  void insertStep_04Success(int executionId);

  void insertStep_04Faild(int executionId);

  void insertStep_99(int executionId);

  void insertScheduleSuccess(int taskId);

  void insertScheduleFaild(int taskId);
}
