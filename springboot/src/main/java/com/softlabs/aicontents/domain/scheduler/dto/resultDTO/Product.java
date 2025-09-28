package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import lombok.Data;

@Data
public class Product {

  String productId;
  String name;
  Integer price;
  String platform;
  String url;
  boolean selected;
}
