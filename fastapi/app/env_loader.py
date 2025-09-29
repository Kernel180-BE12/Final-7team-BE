# fastapi/app/env_loader.py
import os
from pathlib import Path

def load_env() -> None:
    """
    선택: .env 파일을 쓰고 싶으면 python-dotenv 설치 후 아래 두 줄 주석 해제
    """
    # from dotenv import load_dotenv
    # load_dotenv(Path(__file__).resolve().parent / ".env")

def require(key: str, default=None, cast=None):
    v = os.getenv(key, default)
    if v is None:
        return None
    if cast is None:
        return v
    try:
        return cast(v)
    except Exception:
        return v
