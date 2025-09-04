from fastapi import FastAPI

# FastAPI 앱 생성
app = FastAPI(
    title="FastAPI Service",
    description="FastAPI + Docker + Nginx 프로젝트",
    version="1.0.0"
)

# 헬스체크 API
@app.get("/health")
async def health_check():
    return {"status": "ok"}

# 샘플 API
@app.get("/hello")
async def hello():
    return {"message": "Hello, FastAPI!"}