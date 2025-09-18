# app_test.py
# 목적: 운영 고려된 최소 LLM 백엔드 (LangChain)
# 기능: /health, /v1/generate/post, /dry-run
# 포함: JSON 스키마 고정, 가드레일, 타임아웃/재시도, 폴백, NDJSON 로그
# 패치: (1) evidence 검증前 자동 보강 (2) 실패 로그 reason 필드 추가

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from dotenv import load_dotenv
from pathlib import Path
import os, re, json, time, statistics, random, math

# LangChain
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import PydanticOutputParser

# ──────────────────────────────────────────────────────────────────────────────
# 환경 변수/기본 설정
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
MODEL_NAME = os.getenv("LLM_MODEL", "gpt-4o-mini")
LLM_TIMEOUT = float(os.getenv("LLM_TIMEOUT_SEC", "20"))  # LLM 호출 타임아웃(초)
RETRY_MAX = int(os.getenv("LLM_RETRY_MAX", "2"))         # 429/5xx 재시도 횟수
LOG_DIR = Path(os.getenv("LOG_DIR", "./logs")); LOG_DIR.mkdir(parents=True, exist_ok=True)
LOG_PATH = LOG_DIR / "requests.ndjson"

app = FastAPI(title="AI Content Service (LangChain Stable)", version="1.0.0")

# ──────────────────────────────────────────────────────────────────────────────
# Pydantic 스키마 (JSON 고정): title, meta_description, hashtags[], body_markdown, evidence[]
class ProductBrief(BaseModel):
    product_name: str
    source_url: Optional[str] = None
    price: Optional[str] = None
    keywords: List[str] = Field(default_factory=list)

class PostDraft(BaseModel):
    title: str
    meta_description: str
    hashtags: List[str]
    body_markdown: str
    evidence: List[str] = Field(default_factory=list)  # 출처/근거(링크/문장 등)
    version: str = "1.0.0"                              # 응답 스키마 버전

# ──────────────────────────────────────────────────────────────────────────────
# LangChain 구성: Prompt + LLM + Parser
SYSTEM_MSG = """You are a Korean e-commerce copywriter.
- Use Korean only.
- Be specific and avoid hallucinations.
- Do NOT infer unseen brands, model names, or numeric specs. If unknown, write "[미상]".
- Return ONLY JSON (no code fences)."""

parser = PydanticOutputParser(pydantic_object=PostDraft)

GEN_PROMPT = ChatPromptTemplate.from_messages(
    [
        ("system", SYSTEM_MSG),
        ("user", """Generate a promotional blog post with the following constraints.

Product Context:
- Name: {name}
- Price: {price}
- Keywords: {keywords_str}
- Source: {url}

Hard Constraints:
- Title: <= 60 Korean characters
- Meta description: <= 155 Korean characters
- Hashtags: 2~6 items (no spaces)
- Body: Markdown with H2/H3 and bullet points
- If a number/brand/spec is not explicitly provided in the input, write "[미상]" instead of guessing.
- Add the source URL at the end of the body if available.
- Provide "evidence" array listing explicit sources or lines used (prefer source_url if present).

Return JSON matching exactly:
{format_instructions}
"""),
    ]
)

FIX_PROMPT = ChatPromptTemplate.from_messages(
    [
        ("system", SYSTEM_MSG),
        ("user", """Fix the JSON draft to satisfy ALL constraints.

Draft (JSON):
{draft_json}

Violations:
{violations}

Return JSON with this exact schema:
{format_instructions}
"""),
    ]
)

llm = ChatOpenAI(
    model=MODEL_NAME,
    temperature=0.2,     # 결정론 강화
    timeout=LLM_TIMEOUT, # 호출 타임아웃
    max_retries=0,       # 내부 재시도 끔(우리가 명시적으로 제어)
    api_key=OPENAI_API_KEY,
)

gen_prompt = GEN_PROMPT
fix_prompt = FIX_PROMPT

# ──────────────────────────────────────────────────────────────────────────────
# 가드레일(검증) 규칙
MAX_TITLE = 60
MAX_META = 155
MIN_HASHTAGS, MAX_HASHTAGS = 2, 6
BANNED = ["100% 보장", "무조건", "전부 다", "세계 최고", "절대"]

def validate_draft(d: PostDraft, brief: ProductBrief) -> List[str]:
    errs: List[str] = []
    if len(d.title) > MAX_TITLE:
        errs.append(f"title <= {MAX_TITLE}")
    if len(d.meta_description) > MAX_META:
        errs.append(f"meta_description <= {MAX_META}")
    if not (MIN_HASHTAGS <= len(d.hashtags) <= MAX_HASHTAGS):
        errs.append(f"hashtags count {MIN_HASHTAGS}~{MAX_HASHTAGS}")
    combined = f"{d.title}\n{d.body_markdown}"
    if any(w in combined for w in BANNED):
        errs.append("banned words present")
    # 출처 포함
    if brief.source_url and brief.source_url not in d.body_markdown:
        errs.append("source_url must be included in body_markdown")
    # 입력에 없는 가격인데 숫자/통화 표현 등장 → 금지
    if not brief.price and re.search(r"(₩|\d{1,3}(?:,\d{3})+|\d+\s?원)", d.body_markdown or ""):
        errs.append("numeric price present without input price -> use [미상]")
    # 키워드 최소 1개 등장
    if brief.keywords and not any(k in (d.body_markdown or "") for k in brief.keywords):
        errs.append("at least one keyword must appear in body_markdown")
    # evidence 최소 1개 (가능하면 source_url 포함)
    if not d.evidence:
        errs.append("evidence must contain at least one item")
    elif brief.source_url and brief.source_url not in d.evidence:
        errs.append("evidence should include source_url if available")
    return errs

def ensure_evidence(draft: PostDraft, brief: ProductBrief) -> None:
    """
    검증 전에 evidence 배열을 안전 보강한다.
    - source_url이 있으면 body_markdown/ evidence에 반영.
    - draft.evidence가 None인 경우 빈 배열로 초기화.
    """
    if draft.evidence is None:
        draft.evidence = []
    if brief.source_url:
        if brief.source_url not in (draft.body_markdown or ""):
            draft.body_markdown = (draft.body_markdown or "") + f"\n\n자세한 내용: {brief.source_url}"
        if brief.source_url not in draft.evidence:
            draft.evidence.append(brief.source_url)

# ──────────────────────────────────────────────────────────────────────────────
# NDJSON 로깅 (요청당 한 줄)
def log_ndjson(**kwargs):
    LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    with LOG_PATH.open("a", encoding="utf-8") as f:
        f.write(json.dumps(kwargs, ensure_ascii=False) + "\n")

# ──────────────────────────────────────────────────────────────────────────────
# LLM 호출 유틸: 재시도(지수 백오프) + 타임아웃 + 토큰/레이턴시 메타 + 폴백
def _call_llm_with_parser(messages, parser_: PydanticOutputParser) -> (Optional[PostDraft], Dict[str, Any]):
    """
    messages: ChatPromptTemplate.format_messages(...) 결과 (BaseMessage 리스트)
    return: (PostDraft or None, meta dict)
    """
    retries = 0
    backoff = 0.6  # seconds
    last_err = None
    total_start = time.perf_counter()
    token_in = token_out = None

    while True:
        try:
            start = time.perf_counter()
            ai_msg = llm.invoke(messages)  # LangChain ChatOpenAI: BaseMessage 리스트 입력
            # 단일 호출 지연
            latency_ms = (time.perf_counter() - start) * 1000

            # 토큰 사용량(있을 때만)
            meta = ai_msg.response_metadata or {}
            usage = meta.get("token_usage") or meta.get("usage") or {}
            token_in = usage.get("prompt_tokens")
            token_out = usage.get("completion_tokens")

            # 파싱
            content = ai_msg.content
            draft: PostDraft = parser_.parse(content)
            total_latency_ms = (time.perf_counter() - total_start) * 1000
            return draft, {
                "latency_ms": round(total_latency_ms, 2),
                "retries": retries,
                "fallback_used": False,
                "tokens_prompt": token_in,
                "tokens_completion": token_out,
            }
        except Exception as e:
            last_err = str(e)
            retriable = any(s in last_err for s in ["429", "rate", "Rate", "quota", "timeout", "5", "Service Unavailable"])
            if retriable and retries < RETRY_MAX:
                retries += 1
                time.sleep(backoff)
                backoff *= 2
                continue
            # 실패 → 폴백
            total_latency_ms = (time.perf_counter() - total_start) * 1000
            return None, {
                "latency_ms": round(total_latency_ms, 2),
                "retries": retries,
                "fallback_used": True,
                "tokens_prompt": token_in,
                "tokens_completion": token_out,
                "error": last_err,
            }

def _fallback_from_brief(brief: ProductBrief) -> PostDraft:
    name = brief.product_name or "[미상]"
    price = brief.price or "[미상]"
    kws = ", ".join(brief.keywords) if brief.keywords else "[미상]"
    src = brief.source_url or "[미상]"
    body = (
        f"## {name} 한눈에 보기\n"
        f"- 가격: {price}\n"
        f"- 특징 키워드: {kws}\n\n"
        f"### 상세 안내\n"
        f"제공된 정보 범위 내에서만 안내합니다. 추가 정보는 [미상]으로 표기되었습니다.\n\n"
        f"자세한 내용: {src}"
    )
    return PostDraft(
        title=f"[안전폴백] {name} 소개",
        meta_description=f"{name} 기본 정보를 요약합니다.",
        hashtags=["#정보요약", "#안전폴백"],
        body_markdown=body,
        evidence=[src] if src != "[미상]" else ["[미상]"],
        version="1.0.0",
    )

# ──────────────────────────────────────────────────────────────────────────────
# 엔드포인트
@app.get("/health")
def health():
    return {"ok": True}

@app.post("/v1/generate/post", response_model=PostDraft)
def generate_post(brief: ProductBrief):
    if not OPENAI_API_KEY:
        raise HTTPException(500, "OPENAI_API_KEY not configured")

    # 1) 생성 프롬프트 메시지 조립
    inputs = {
        "name": brief.product_name,
        "price": brief.price or "[미상]",
        "keywords_str": ", ".join(brief.keywords) if brief.keywords else "[미상]",
        "url": brief.source_url or "[미상]",
        "format_instructions": parser.get_format_instructions(),
    }
    messages = gen_prompt.format_messages(**inputs)

    # 2) 호출(재시도/타임아웃/토큰/레이턴시 메타)
    draft, meta = _call_llm_with_parser(messages, parser)

    # 3) evidence 보강 (검증 이전)
    if draft is not None:
        ensure_evidence(draft, brief)

    # 4) 폴백 처리 + 보강
    if draft is None:
        draft = _fallback_from_brief(brief)
    ensure_evidence(draft, brief)

    # 5) 가드레일 검사 → 수정(1회 시도) → 실패 시 422
    errs = validate_draft(draft, brief)
    if errs:
        fix_inputs = {
            "draft_json": draft.model_dump_json(ensure_ascii=False),
            "violations": "\n".join(f"- {e}" for e in errs),
            "format_instructions": parser.get_format_instructions(),
        }
        fix_msgs = fix_prompt.format_messages(**fix_inputs)
        fixed, meta_fix = _call_llm_with_parser(fix_msgs, parser)

        if fixed is not None:
            draft = fixed
            ensure_evidence(draft, brief)  # 수정본도 검증前 보강
            errs2 = validate_draft(draft, brief)
            if errs2:
                log_ndjson(endpoint="/v1/generate/post", ok=False, reason="GUARDRAIL_FAILED", violations=errs2, **meta)
                raise HTTPException(422, {"error_code":"GUARDRAIL_FAILED","violations":errs2})
            meta = meta_fix
        else:
            # 수정 실패지만 폴백이 이미 적용된 경우 통과 가능 (검증은 아래에서 다시 진행되지 않음)
            pass

    # 6) NDJSON 로그
    reason = "FALLBACK" if meta.get("fallback_used") else ("RETRIED" if meta.get("retries", 0) > 0 else None)
    log_ndjson(
        endpoint="/v1/generate/post",
        ok=True,
        reason=reason,
        product_name=brief.product_name,
        latency_ms=meta.get("latency_ms"),
        retries=meta.get("retries"),
        fallback_used=meta.get("fallback_used"),
        tokens_prompt=meta.get("tokens_prompt"),
        tokens_completion=meta.get("tokens_completion"),
        model=MODEL_NAME,
    )
    return draft

# ──────────────────────────────────────────────────────────────────────────────
# /dry-run: 샘플 5~10건 스모크 → 성과표(p50/p95, 성공률) + 미리보기
SAMPLE_PRODUCTS = [
    ("에코백", "₩15,900", ["보냉","휴대"]),
    ("텀블러", None, ["보온","세척"]),
    ("러닝화", "₩59,000", ["쿠셔닝","통기성"]),
    ("무선이어폰", None, ["저지연","배터리"]),
    ("폴딩우산", "₩9,900", ["방수","경량"]),
]

class DryRunResult(BaseModel):
    total: int
    success: int
    success_rate: float
    p50_latency_ms: Optional[float]
    p95_latency_ms: Optional[float]
    preview: List[Dict[str, Any]]

@app.post("/dry-run", response_model=DryRunResult)
def dry_run(n: int = 5):
    if n < 1: n = 1
    if n > 10: n = 10
    cases = random.sample(SAMPLE_PRODUCTS, k=min(n, len(SAMPLE_PRODUCTS)))

    latencies: List[float] = []
    previews: List[Dict[str, Any]] = []
    success = 0

    for name, price, kws in cases:
        brief = ProductBrief(product_name=name, price=price, keywords=kws, source_url="https://example.com/item")
        try:
            start = time.perf_counter()
            _ = generate_post(brief)  # 동일 파이프라인 사용(로그/가드레일 포함)
            lat = (time.perf_counter() - start) * 1000
            latencies.append(lat)
            success += 1
            previews.append({"product": name, "latency_ms": round(lat, 2)})
        except Exception as e:
            previews.append({"product": name, "error": str(e)})

    p50 = round(statistics.median(latencies), 2) if latencies else None
    p95 = None
    if latencies:
        sorted_lat = sorted(latencies)
        idx = min(len(sorted_lat)-1, math.ceil(0.95*len(sorted_lat))-1)
        p95 = round(sorted_lat[idx], 2)

    return DryRunResult(
        total=len(cases),
        success=success,
        success_rate=round(success/len(cases), 2),
        p50_latency_ms=p50,
        p95_latency_ms=p95,
        preview=previews[:min(3, len(previews))],  # README 캡처용 2~3개만
    )
