package com.softlabs.aicontents.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.softlabs.aicontents")
public class MyBatisConfig {}

