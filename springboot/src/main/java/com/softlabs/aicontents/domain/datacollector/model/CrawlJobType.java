package com.softlabs.aicontents.domain.datacollector.model;

public enum CrawlJobType {
  AUTOMATIC("자동"),
  MANUAL("수동");

  private final String description;

  CrawlJobType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}