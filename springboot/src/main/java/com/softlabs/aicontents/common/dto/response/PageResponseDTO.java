package com.softlabs.aicontents.common.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class PageResponseDTO<T> {
  private List<T> content;
  private int currentPage;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;

  public PageResponseDTO(List<T> content, int currentPage, int pageSize, long totalElements) {
    this.content = content;
    this.currentPage = currentPage;
    this.pageSize = pageSize;
    this.totalElements = totalElements;
    this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    this.hasNext = currentPage < totalPages;
    this.hasPrevious = currentPage > 1;
  }
}
