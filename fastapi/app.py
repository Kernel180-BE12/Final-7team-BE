#app.py
#FastAPI 최소 서버: /health, /v1/generate/post
#목적: Spring Boot 와의 연결 테스트 및 LLM 모듈 자리잡기
#현재는 LLM  호출 없이 mock 응답 반환

from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import List, Optional


#App Instance - FastAPI 앱의 진입점 객체
app = FastAPI(
    title="AI Content Service",
    description="Generates promotional blog content from a crawled product brief.",
    version="0.1.0",
)

# ====== 1) 요청/응답 스키마 ======
#Define data model(schema) - Pydantic
class ProductBrief(BaseModel):
    """
    크롤링/전처리 결과에서 LLM에 필요한 최소 필드.
    실제 스키마가 정해지면 여기를 확장/수정하면 됨.
    """
    product_name: str
    source_url: Optional[str] = None #필드가 없어도/NULL이어도 허용
    price: Optional[str] = None
    keywords: List[str] = Field(default_factory=list)

class PostDraft(BaseModel):
    """
    LLM(현재 mock)이 생성한 블로그 초안
    """
    title: str
    meta_description: str
    hashtags: List[str]
    body_markdown: str

# ====== 2) 헬스체크 ======
@app.get("/health")
def health():
    """
    헬스체크 엔드포인트
    - 배포/연결/모니터링에서 살아있는지 확인할 때 사용
    - 반환값은 단순한 JSON
    """
    return {"ok":True} #딕셔너리 → FastAPI가 자동으로 JSON으로 변환해 응답

# ====== 3) 생성 엔드포인트 (현재 mock) ======
@app.post("/v1/generate/post", response_model=PostDraft)
def generate_post(brief: ProductBrief):
    """
    블로그 프로모션용 초안 생성
    - 지금은 LLM을 호출하지 않고 샘플 응답을 반환
    - 이후 Gemini 연결 시 이 로직을 교체
    """
    # 간단한 규칙 기반 타이틀/본문 생성 (연결 검증용)
    title = f"[sample] {brief.product_name} promotion"
    meta = "이 제품의 주요 장점을 간단히 소개합니다"
    hashtags = ["#추천", "#가성비"]

    #keywords를 본문에 사용 (데모)
    kw_line = ""
    if brief.keywords:
        kw_line = "키워드: "  + ",".join(brief.keywords)

        body = (
            "## 왜 좋은가요?\n"
            "- 합리적 가격\n"
            "- 실용적 기능\n\n"
            f"{kw_line}\n\n"
            "자세한 정보는 링크를 확인하세요."
        )

        return PostDraft(
            title=title,
            meta_description=meta,
            hashtags=hashtags,
            body_markdown=body
        )

