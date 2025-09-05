import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv(dotenv_path="../.env")

# 환경변수에서 데이터베이스 정보 가져오기
DB_HOST = os.getenv("DB_HOST")
DB_SERVICE = os.getenv("DB_SERVICE") 
DB_USERNAME = os.getenv("DB_USERNAME")
DB_PASSWORD = os.getenv("DB_PASSWORD")

# Oracle 데이터베이스 연결 URL 생성
DATABASE_URL = f"oracle+oracledb://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:1521/{DB_SERVICE}"

# SQLAlchemy 엔진 생성
engine = create_engine(
    DATABASE_URL,
    pool_size=10,
    max_overflow=20,
    pool_pre_ping=True,
    echo=True  # 개발 시에만 사용, 프로덕션에서는 False
)

# 세션 생성기
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base 클래스 생성 (모델들의 기본 클래스)
Base = declarative_base()

# 데이터베이스 세션 의존성
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()