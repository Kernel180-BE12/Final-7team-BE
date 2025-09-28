package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import java.util.List;
import lombok.Data;

@Data
public class StageResults {
  List<Keyword> keywords;
  List<Product> products;
  Content content;
  PublishingStatus publishingStatus;
}
