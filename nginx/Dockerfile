# Nginx 전체 설정을 정의하는 메인 설정 파일 (전역 옵션, 로그, 워커 프로세스 등을 관리)

# Nginx 공식 경량 이미지 사용
FROM nginx:1.27-alpine

# 컨테이너 내부 작업 디렉토리 설정
WORKDIR /etc/nginx

# 기존 Nginx 설정 삭제
RUN rm -rf /etc/nginx/conf.d/*

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf
COPY conf.d /etc/nginx/conf.d

# Nginx 포트 오픈
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
