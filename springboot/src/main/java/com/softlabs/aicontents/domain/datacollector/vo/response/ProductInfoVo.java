package com.softlabs.aicontents.domain.datacollector.vo.response;

import lombok.Data;

@Data
public class ProductInfoVo {
  private Long productId;
  private String keyword;
  private String productName;
  private String productNameCn;
  private String price;
  private String description;
  private String rating;
  private String productUrl;
  private String imageUrl;
  private String localImagePath;
  private String crawlType;
  private String ocrText;
  private String createdAt;
}