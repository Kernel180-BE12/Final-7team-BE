package com.softlabs.aicontents.domain.testexample.entity;

public class TestExample {
    private Long id;
    private String testData;
    
    public TestExample() {}
    
    public TestExample(String testData) {
        this.testData = testData;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTestData() {
        return testData;
    }
    
    public void setTestData(String testData) {
        this.testData = testData;
    }
}