package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import java.util.List;
import lombok.Data;

@Data
public class Content {
  String title;
  String summary;
  String content;
  List<String> tags;
}
