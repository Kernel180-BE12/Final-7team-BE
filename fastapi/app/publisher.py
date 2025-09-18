import json
import os
import re
from fastapi import HTTPException
from playwright.async_api import Browser, TimeoutError as PwTimeout

from . import config
from .schemas import PublishRequest, PublishResponse, PublishStatus


class BlogPostPublisher:
    def __init__(self, browser: Browser):
        self.browser = browser

    async def publish(self, req: PublishRequest) -> PublishResponse:
        context = await self.browser.new_context(
            storage_state=config.STATE_PATH if os.path.exists(config.STATE_PATH) else None,
            locale="ko-KR",
        )
        page = await context.new_page()
        # 모든 동작의 기본 대기 시간을 60초
        page.set_default_timeout(60_000)

        try:
            # 1) 글쓰기 페이지 이동, 네트워크가 안정될 때까지 대기
            print("글쓰기 페이지로 이동합니다...")
            await page.goto(config.WRITE_URL, wait_until="networkidle", timeout=90_000)
            # await page.screenshot(path="debug_01_page_loaded.png", full_page=True)

            # 로그인 페이지로 리디렉션되었는지 확인
            if "nid.naver.com" in page.url:
                raise HTTPException(
                    status_code=401,
                    detail="로그인이 필요합니다. HEADLESS=false 옵션으로 한번 실행하여 로그인 세션을 저장하세요."
                )

            # 2) 제목 입력
            if req.title:
                print(f"제목을 입력합니다: {req.title}")
                title_locator = page.locator(config.TITLE_SELECTOR)

                # 1. 입력 영역을 마우스로 클릭하여 활성화
                await title_locator.click()

                # 2. 활성화된 곳에 키보드로 직접 타이핑
                await page.keyboard.type(req.title)

                print("[성공] 제목 입력 완료")
                # await page.screenshot(path="debug_02_title_filled.png", full_page=True)

            # 3) 본문 입력
            if req.markdown:
                await page.keyboard.press("Enter")
                await page.wait_for_timeout(200)

                print("본문을 입력합니다...")
                # iframe 요소 자체를 먼저 기다림
                await page.wait_for_selector(config.IFRAME_SELECTOR, timeout=30_000)

                iframe_element = await page.wait_for_selector(config.IFRAME_SELECTOR)
                frame = await iframe_element.content_frame()
                await frame.wait_for_load_state("domcontentloaded")

                # JavaScript로 직접 포커스 설정 후 입력
                print("JavaScript로 포커스 설정 후 키보드 입력...")
                await frame.evaluate("document.body.focus()")
                await page.wait_for_timeout(500)

                # 본문 내용 입력
                await page.keyboard.type(req.markdown)
                await page.wait_for_timeout(2000)

                # 결과 확인
                print(f"최종 입력된 내용: '{req.markdown}'")
                print("[성공] 본문 입력 완료")
                # await page.screenshot(path="debug_03_body_filled.png", full_page=True)
            # 4) 1차 발행 버튼 클릭
            print("1차 발행 버튼 클릭")
            await page.locator(config.EDITOR_PUBLISH_BUTTON_SELECTOR).click()

            # 5) 발행 팝업 창이 나타날 때까지 대기
            print("발행 팝업 창을 기다립니다...")
            await page.wait_for_selector(config.MODAL_TAGS_SELECTOR)
            print("[성공] 발행 팝업 창 로드 완료")
            # await page.screenshot(path="debug_04_modal_opened.png", full_page=True)

            # 6) 팝업 창에서 태그 입력
            if req.hashtag:
                print(f"태그를 입력합니다: {req.hashtag}")
                tag_locator = page.locator(config.MODAL_TAGS_SELECTOR)
                await tag_locator.fill(req.hashtag)
                await tag_locator.press("Enter")
                print("[성공] 태그 입력 완료")
                # await page.screenshot(path="debug_05_tags_filled.png", full_page=True)

            # 7) 팝업 창의 최종 발행 버튼 클릭
            print("최종 발행 버튼을 클릭합니다...")
            await page.locator(config.MODAL_PUBLISH_BUTTON_SELECTOR).click()

            # 8) 발행 완료 확인 및 최종 URL 추출
            print("발행 완료 페이지 로딩을 기다립니다...")
            await page.wait_for_load_state("networkidle", timeout=90_000)

            blog_url = page.url
            print(f"[성공] 최종 발행된 URL: {blog_url}")
            # await page.screenshot(path="debug_06_final_page.png", full_page=True)

            post_id = self._extract_post_id(blog_url)
            raw = {"url": blog_url, "postId": post_id}

            return PublishResponse(
                publishStatus=PublishStatus.SUCCESS,
                blogPostId=post_id,
                blogUrl=blog_url,
                publishResponse=json.dumps(raw, ensure_ascii=False),
            )

        except (PwTimeout, Exception) as e:
            error_message = f"발행 작업 중 오류 발생: {e}"
            print(f"[오류] {error_message}")
            await page.screenshot(path="debug_error.png", full_page=True)
            return PublishResponse(
                publishStatus=PublishStatus.FAILED,
                errorMessage=error_message
            )
        finally:
            print("세션을 저장하고 브라우저 컨텍스트를 닫습니다.")
            await context.storage_state(path=config.STATE_PATH)
            await context.close()

    def _extract_post_id(self, url: str):
        # 예시: https://blog.naver.com/{blogId}/{postId}
        m = re.search(r"/(\d+)(?:\?.*)?$", url or "")
        return m.group(1) if m else None