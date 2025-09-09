//package com.softlabs.aicontents.domain.testexample.service;
//
//import com.softlabs.aicontents.domain.testexample.entity.TestExample;
//import com.softlabs.aicontents.domain.testexample.mapper.TestExampleMapper;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TestExampleService {
//
//    private final TestExampleMapper testExampleMapper;
//
//    public TestExampleService(TestExampleMapper testExampleMapper) {
//        this.testExampleMapper = testExampleMapper;
//    }
//
//    public void createTestExample(String testData) {
//        TestExample testExample = new TestExample(testData);
//        testExampleMapper.insertTestExample(testExample);
//    }
//}