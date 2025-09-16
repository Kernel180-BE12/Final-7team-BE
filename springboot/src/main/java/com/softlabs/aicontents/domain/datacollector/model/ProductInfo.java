package com.softlabs.aicontents.domain.datacollector.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
public class ProductInfo {
  private String keyword;
  private String productName;
  private String price;
  private String productNameCn;
  private String description;
  private String rating;
  private String productUrl;
  private String crawlType;
  private String createdAt;

  public ProductInfo() {}

  public String getCrawledAt() {
    return createdAt;
  }

  public void setCrawledAt(String crawledAt) {
    this.createdAt = crawledAt;
  }

  public void setCurrentTime() {
    this.createdAt =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== 상품 정보 ===\n");
    sb.append("검색 키워드: ").append(keyword).append("\n");
    sb.append("상품명: ").append(productName).append("\n");
    sb.append("중국어 상품명: ").append(productNameCn).append("\n");
    sb.append("현재가격: ").append(price).append("\n");
    sb.append("상품설명: ").append(description).append("\n");
    sb.append("평점: ").append(rating).append("\n");
    sb.append("상품URL: ").append(productUrl).append("\n");
    sb.append("크롤링 타입: ").append(crawlType).append("\n");
    sb.append("수집일시: ").append(createdAt).append("\n");
    sb.append("==================\n");
    return sb.toString();
  }
}