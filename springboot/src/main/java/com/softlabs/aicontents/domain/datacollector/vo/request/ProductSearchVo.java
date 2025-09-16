package com.softlabs.aicontents.domain.datacollector.vo.request;

import lombok.Data;

@Data
public class ProductSearchVo {
  private String keyword;
  private String crawlType;
  private Integer limit;
  private Integer offset;
}