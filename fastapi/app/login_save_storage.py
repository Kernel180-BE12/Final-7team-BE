# tools/login_save_storage.py
import asyncio, os
from pathlib import Path
from playwright.async_api import async_playwright

STATE_PATH = Path("storage/naver_state.json")
STATE_PATH.parent.mkdir(parents=True, exist_ok=True)

async def main():
    async with async_playwright() as pw:
        # 채널 지정 안 함 → Chromium 기본 (서버도 동일하게 사용할 것)
        browser = await pw.chromium.launch(headless=False)
        ctx = await browser.new_context(locale="ko-KR", timezone_id="Asia/Seoul")
        page = await ctx.new_page()
        # 로그인 페이지로 진입
        await page.goto("https://nid.naver.com/user2/help/myInfo?lang=ko")
        print("[INFO] 이 창에서 네이버 로그인(2단계 포함)을 완료하세요.")
        print("로그인 후 아무 키나 터미널에 입력하면 저장합니다...")
        input()

        # 세션 저장
        await ctx.storage_state(path=str(STATE_PATH))
        print(f"[OK] storage_state 저장: {STATE_PATH.resolve()}")

        await ctx.close()
        await browser.close()

if __name__ == "__main__":
    asyncio.run(main())
