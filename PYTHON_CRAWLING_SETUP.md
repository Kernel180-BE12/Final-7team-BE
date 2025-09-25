# Python 크롤링 설치 매뉴얼

## 필수 준비사항

**Python 3.11 이상 필요** - [python.org](https://www.python.org)에서 다운로드

### 1. 프로젝트 이동(powershell)
```powershell
cd Final-7team-BE (본인 프로젝트 위치)
cd fastapi
```

### 2. 패키지 설치
```powershell
pip install oracledb
pip install playwright
python -m playwright install
pip install pillow opencv-python pytesseract
```





### 3.  가상환경 생성 (선택사항) 
```powershell
# 가상환경 생성
python -m venv venv

# 가상환경 활성화
venv\Scripts\activate
```


### 4. 가상 환경 실행
```powershell
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 5. 확인
브라우저에서 `http://localhost:8000/docs` 접속
