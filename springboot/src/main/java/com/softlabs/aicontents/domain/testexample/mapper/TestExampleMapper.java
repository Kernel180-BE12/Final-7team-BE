package com.softlabs.aicontents.domain.testexample.mapper;

import com.softlabs.aicontents.domain.testexample.entity.TestExample;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestExampleMapper {
  @org.apache.ibatis.annotations.Select("SELECT 1 FROM DUAL")
  Integer ping();

  void insertTestExample(TestExample testExample);
}
