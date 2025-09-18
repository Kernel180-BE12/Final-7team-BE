package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import lombok.Data;

@Data
public class ProgressResult {

  KeywordExtraction keywordExtraction;
  ProductCrawling productCrawling;
  ContentGeneration contentGeneration;
  ContentPublishing contentPublishing;
}
