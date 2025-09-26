import asyncio, sys
if sys.platform.startswith("win"):
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

from fastapi import FastAPI, Depends, HTTPException, Request
from sqlalchemy.orm import Session
from sqlalchemy import text
from datetime import datetime
from . import models, schemas, config
from .database import SessionLocal, engine, get_db
from playwright.async_api import async_playwright
from .schemas import PublishRequest, PublishResponse
from .publisher import BlogPostPublisher



# FastAPI 앱 생성
app = FastAPI(
    title="FastAPI Service with Oracle RDS",
    description="FastAPI + Docker + Nginx + Oracle RDS 프로젝트",
    version="1.0.0"
)

# 기본 헬스체크 API
@app.get("/health")
async def health_check():
    return {"status": "ok"}

# 데이터베이스 연결 테스트를 포함한 헬스체크 API
@app.get("/health/db", response_model=schemas.HealthCheckResponse)
async def database_health_check(db: Session = Depends(get_db)):
    try:
        # 간단한 쿼리로 데이터베이스 연결 테스트
        db.execute(text("SELECT 1 FROM DUAL"))
        
        return schemas.HealthCheckResponse(
            status="ok",
            database_status="connected",
            timestamp=datetime.now()
        )
    except Exception as e:
        raise HTTPException(
            status_code=503, 
            detail=f"Database connection failed: {str(e)}"
        )

# 샘플 API - 사용자 생성
@app.post("/users/", response_model=schemas.User)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = models.User(username=user.username, email=user.email)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# 샘플 API - 사용자 목록 조회
@app.get("/users/", response_model=list[schemas.User])
def read_users(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    users = db.query(models.User).offset(skip).limit(limit).all()
    return users

# 샘플 API
@app.get("/hello")
async def hello():
    return {"message": "Hello, FastAPI with Oracle RDS!"}

# 발행 API
@app.post("/publish/healthy")
async def publish_check(req: Request):
    raw = await req.body()
    print("=== FASTAPI REQ BODY ===")
    print(raw.decode("utf-8"))
    return {"publishStatus": "SUCCESS"}

@app.on_event("startup")
async def startup():
    print("데이터베이스 테이블을 생성합니다...")
    # 동기 함수를 비동기 환경에서 안전하게 실행
    await asyncio.to_thread(models.Base.metadata.create_all, bind=engine)
    print("테이블 생성 완료.")

# Playwright 시작
    print("Playwright를 시작합니다...")
    app.state.playwright = await async_playwright().start()
    app.state.browser = await app.state.playwright.chromium.launch(headless=True)
    print("Playwright 시작 완료.")
@app.on_event("shutdown")
async def shutdown():
    await app.state.browser.close()
    await app.state.playwright.stop()

@app.post("/publish", response_model=PublishResponse)
async def publish(req: PublishRequest):
    if not req.aiContentId:
        raise HTTPException(status_code=400, detail="aiContentId is required")

    publisher = BlogPostPublisher(browser=app.state.browser)
    return await publisher.publish(req)
