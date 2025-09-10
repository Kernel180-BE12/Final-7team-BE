package com.softlabs.aicontents.orchestration.enums;

public enum PipelineStep {
  STEP_01("STEP_01", "구글 트렌드 키워드 수집", 1),
  STEP_02("STEP_02", "싸다구몰 상품 수집", 2),
  STEP_03("STEP_03", "AI 콘텐츠 생성", 3),
  STEP_04("STEP_04", "네이버 블로그 업로드", 4);

  private final String code;
  private final String description;
  private final int order;

  PipelineStep(String code, String description, int order) {
    this.code = code;
    this.description = description;
    this.order = order;
  }
}
