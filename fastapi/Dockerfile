# Python 기반 경량 이미지 사용
FROM python:3.11-slim

# 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지 설치 및 pip 업그레이드
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential gcc && \
    pip install --upgrade pip && \
    rm -rf /var/lib/apt/lists/*

# Python 의존성 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# FastAPI 소스 코드 복사
COPY ./app ./app

# FastAPI 포트 오픈
EXPOSE 8000

# Uvicorn 실행 (멀티 프로세스 지원)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "4"]

