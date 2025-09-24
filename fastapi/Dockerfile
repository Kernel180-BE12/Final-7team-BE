# Python 기반 경량 이미지 사용
FROM python:3.11-slim

# 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지(폰트/타임존 포함) 설치 및 pip 업그레이드
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl ca-certificates tzdata fonts-noto-cjk \
    && ln -fs /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && dpkg-reconfigure -f noninteractive tzdata \
    && rm -rf /var/lib/apt/lists/* \
    && pip install --upgrade pip

# Python 의존성 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# === Playwright 추가 (브라우저 & OS deps 한방 설치) ===
RUN pip install --no-cache-dir playwright==1.55.0 && \
    python -m playwright install --with-deps chromium

# FastAPI 소스 코드 복사
COPY ./app ./app

# 런타임 환경변수(로그인 세션 저장 경로 등)
ENV HEADLESS=true \
    STATE_PATH=/app/storage/naver_state.json \
    PYTHONUNBUFFERED=1

# 로그인 세션 저장 폴더
RUN mkdir -p /app/storage

# FastAPI 포트 오픈
EXPOSE 8000

# Uvicorn 실행 (Playwright 안정 위해 워커 1개 권장)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
