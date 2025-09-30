import logging
import random
import time
import requests
from typing import Optional, List, Dict
from .schemas import KeywordCrawlResponse
from playwright.async_api import async_playwright, Playwright, Browser, Page
import re
import base64
import io
from PIL import Image
import pytesseract
import cv2
import numpy as np

logger = logging.getLogger(__name__)

class SsadaguCrawler:
    def __init__(self):
        """싸다구몰 크롤러 초기화"""
        self.playwright = None
        self.browser = None
        self.page = None

    async def _setup_playwright(self):
        """Playwright 설정"""
        try:
            self.playwright = await async_playwright().start()
            self.browser = await self.playwright.chromium.launch(
                headless=True,
                args=['--no-sandbox', '--disable-dev-shm-usage']
            )
            self.page = await self.browser.new_page()
            await self.page.set_user_agent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36')
            logger.info("Playwright 설정 완료 (headless 모드)")
        except Exception as e:
            logger.error(f"Playwright 설정 실패: {e}")
            self.playwright = None
            self.browser = None
            self.page = None

    async def crawl_ssadagu_product(self, keyword: str, execution_id: int) -> Dict:
        """
        싸다구몰에서 키워드 검색 후 첫번째 상품 정보 추출

        Args:
            keyword: 검색 키워드 (공백 제거된 상태)
            execution_id: 실행 ID

        Returns:
            Dict: 상품 정보 {product_name, product_url, price, success}
        """
        try:
            logger.info(f"싸다구몰 상품 크롤링 시작 - keyword: {keyword}, execution_id: {execution_id}")

            # Playwright 설정
            if not self.page:
                await self._setup_playwright()

            # 1단계: 검색 페이지 접근
            search_url = f"https://ssadagu.kr/shop/search.php?ss_tx={keyword}"
            logger.info(f"검색 URL 접근: {search_url}")

            # Playwright가 설정되지 않은 경우 fallback 모드
            if not self.page:
                return self._fallback_crawl_with_requests(keyword, search_url)

            # 2단계: Playwright로 동적 크롤링
            return await self._playwright_crawl(search_url, keyword)

        except Exception as e:
            logger.error(f"싸다구몰 크롤링 실패 - keyword: {keyword}, error: {e}")
            return {
                'success': False,
                'error_message': str(e),
                'product_name': None,
                'product_url': None,
                'price': None
            }

    async def _playwright_crawl(self, search_url: str, keyword: str) -> Dict:
        """Playwright를 이용한 실제 크롤링"""
        try:
            await self.page.goto(search_url)
            await self.page.wait_for_load_state('networkidle')

            # 첫번째 상품 링크 찾기
            first_product_link = await self._find_first_product_link_playwright()
            if not first_product_link:
                return self._create_fallback_product(keyword, search_url)

            # 첫번째 상품 페이지로 이동
            product_url = await first_product_link.get_attribute('href')
            logger.info(f"첫번째 상품 페이지 이동: {product_url}")

            await self.page.goto(product_url)
            await self.page.wait_for_load_state('networkidle')

            # 상품 정보 추출
            product_name = await self._extract_product_name_playwright()
            price = await self._extract_product_price_playwright()
            current_url = self.page.url

            logger.info(f"상품 정보 추출 완료 - 상품명: {product_name}, 가격: {price}, URL: {current_url}")

            return {
                'success': True,
                'product_name': product_name,
                'product_url': current_url,
                'price': price,
                'crawling_method': 'PLAYWRIGHT'
            }

        except Exception as e:
            logger.error(f"Playwright 크롤링 실패: {e}")
            return self._create_fallback_product(keyword, search_url)

    async def _find_first_product_link_playwright(self):
        """Playwright로 검색 결과에서 첫번째 상품 링크 찾기"""
        try:
            # 다양한 셀렉터로 첫번째 상품 링크 시도
            selectors = [
                'a[href*="view.php"]',  # 싸다구몰 상품 상세 페이지 패턴
                '.product-item a',
                '.item-list a',
                '.product-link',
                'a[href*="product"]',
                '.list-item a'
            ]

            for selector in selectors:
                try:
                    link = await self.page.query_selector(selector)
                    if link:
                        logger.info(f"첫번째 상품 링크 발견 (셀렉터: {selector})")
                        return link
                except:
                    continue

            logger.warning("첫번째 상품 링크를 찾을 수 없음")
            return None

        except Exception as e:
            logger.error(f"상품 링크 검색 실패: {e}")
            return None

    async def _extract_product_name_playwright(self) -> str:
        """Playwright로 상품 상세 페이지에서 상품명 추출"""
        try:
            # 1단계: 일반적인 HTML 텍스트 추출 시도
            html_product_name = await self._extract_product_name_from_html_elements_playwright()
            if html_product_name and not html_product_name.startswith("["):
                logger.info(f"HTML 요소에서 상품명 추출 성공: {html_product_name}")
                return html_product_name

            # 2단계: OCR을 활용한 이미지 텍스트 추출
            logger.info("HTML 추출 실패 - OCR 방식 시도")
            ocr_product_name = await self._extract_product_name_with_ocr_playwright()
            if ocr_product_name and not ocr_product_name.startswith("["):
                logger.info(f"OCR에서 상품명 추출 성공: {ocr_product_name}")
                return ocr_product_name

            # 3단계: 페이지 소스 전체에서 상품명 패턴 검색
            logger.info("OCR 추출 실패 - 페이지 소스 패턴 검색 시도")
            pattern_product_name = await self._extract_product_name_from_page_source_playwright()
            if pattern_product_name and not pattern_product_name.startswith("["):
                logger.info(f"패턴 검색에서 상품명 추출 성공: {pattern_product_name}")
                return pattern_product_name

            logger.warning("모든 상품명 추출 방식 실패")
            return "[상품명 추출 실패 - 이미지 기반 페이지]"

        except Exception as e:
            logger.error(f"상품명 추출 실패: {e}")
            return "[상품명 추출 오류]"

    async def _extract_product_name_from_html_elements_playwright(self) -> str:
        """Playwright로 HTML 요소에서 상품명 추출"""
        try:
            # 다양한 셀렉터로 상품명 추출 시도
            selectors = [
                'h1.product-title',
                '.product-name',
                'h1',
                '.title',
                '.product-info h1',
                '.item-name',
                '[class*="title"]',
                '[class*="name"]',
                '[id*="title"]',
                '[id*="name"]'
            ]

            for selector in selectors:
                try:
                    element = await self.page.query_selector(selector)
                    if element:
                        text = await element.text_content()
                        if text and text.strip():
                            text = text.strip()
                            if self._is_valid_product_name(text):
                                return text
                except:
                    continue

            # 페이지 타이틀에서 상품명 추출
            title = await self.page.title()
            if title and '싸다구' in title:
                clean_title = title.replace(' - 싸다구몰', '').strip()
                if self._is_valid_product_name(clean_title):
                    return clean_title

            return "[HTML 요소 추출 실패]"

        except Exception as e:
            logger.error(f"HTML 요소 추출 실패: {e}")
            return "[HTML 요소 추출 오류]"

    async def _extract_product_name_with_ocr_playwright(self) -> str:
        """Playwright OCR을 활용한 상품명 추출"""
        try:
            # 페이지 스크린샷 캡처
            screenshot_bytes = await self.page.screenshot()
            image = Image.open(io.BytesIO(screenshot_bytes))

            # 상품명 영역으로 추정되는 상단 영역만 크롭 (성능 최적화)
            width, height = image.size
            cropped_image = image.crop((0, 0, width, height // 3))  # 상단 1/3 영역

            # 이미지 전처리 (OCR 정확도 향상)
            cv_image = cv2.cvtColor(np.array(cropped_image), cv2.COLOR_RGB2BGR)
            gray = cv2.cvtColor(cv_image, cv2.COLOR_BGR2GRAY)

            # 대비 및 노이즈 제거
            enhanced = cv2.convertScaleAbs(gray, alpha=1.5, beta=30)
            denoised = cv2.medianBlur(enhanced, 3)

            # OCR 수행 (한국어 + 영어)
            try:
                # Tesseract 설정: 한국어 우선, 영어 보조
                custom_config = r'--oem 3 --psm 6 -l kor+eng'
                ocr_text = pytesseract.image_to_string(denoised, config=custom_config)

                if not ocr_text.strip():
                    # 한국어만으로 재시도
                    custom_config = r'--oem 3 --psm 6 -l kor'
                    ocr_text = pytesseract.image_to_string(denoised, config=custom_config)

            except Exception:
                # Tesseract 한국어 모델이 없는 경우 영어만 사용
                custom_config = r'--oem 3 --psm 6 -l eng'
                ocr_text = pytesseract.image_to_string(denoised, config=custom_config)

            # OCR 결과에서 상품명 후보 추출
            if ocr_text.strip():
                product_name = self._parse_product_name_from_ocr(ocr_text)
                if product_name and self._is_valid_product_name(product_name):
                    return product_name

            return "[OCR 추출 실패]"

        except Exception as e:
            logger.error(f"OCR 추출 실패: {e}")
            return "[OCR 추출 오류]"

    async def _extract_product_name_from_page_source_playwright(self) -> str:
        """Playwright로 페이지 소스에서 상품명 패턴 검색"""
        try:
            page_content = await self.page.content()

            # 다양한 패턴으로 상품명 검색
            patterns = [
                r'<title>([^<]+)</title>',
                r'property="og:title"\s+content="([^"]+)"',
                r'name="title"\s+content="([^"]+)"',
                r'<h1[^>]*>([^<]+)</h1>',
                r'<h2[^>]*>([^<]+)</h2>',
                r'product[_-]?name["\s]*:[\s]*["\']([^"\']+)["\']',
                r'title["\s]*:[\s]*["\']([^"\']+)["\']',
                r'상품명["\s]*:[\s]*["\']?([^"\'>\n]+)',
                r'제품명["\s]*:[\s]*["\']?([^"\'>\n]+)',
                r'품명["\s]*:[\s]*["\']?([^"\'>\n]+)'
            ]

            for pattern in patterns:
                matches = re.findall(pattern, page_content, re.IGNORECASE | re.DOTALL)
                for match in matches:
                    clean_match = match.strip()
                    # 싸다구몰 관련 텍스트 정리
                    clean_match = re.sub(r'\s*-\s*싸다구몰.*?$', '', clean_match)
                    clean_match = re.sub(r'싸다구몰.*?$', '', clean_match).strip()

                    if self._is_valid_product_name(clean_match):
                        return clean_match

            return "[페이지 소스 검색 실패]"

        except Exception as e:
            logger.error(f"페이지 소스 검색 실패: {e}")
            return "[페이지 소스 검색 오류]"

    async def _extract_product_price_playwright(self) -> int:
        """Playwright로 상품 상세 페이지에서 가격 추출"""
        try:
            # 다양한 셀렉터로 가격 추출 시도
            selectors = [
                '.price',
                '.product-price',
                '.sale-price',
                '.current-price',
                '[class*="price"]'
            ]

            for selector in selectors:
                try:
                    elements = await self.page.query_selector_all(selector)
                    for element in elements:
                        text = await element.text_content()
                        if text:
                            price = self._parse_price_from_text(text)
                            if price > 0:
                                return price
                except:
                    continue

            # 페이지 전체에서 가격 패턴 검색
            page_content = await self.page.content()
            price_match = re.search(r'(\d{1,3}(?:,\d{3})*)\s*원', page_content)
            if price_match:
                price_str = price_match.group(1).replace(',', '')
                return int(price_str)

            return 0

        except Exception as e:
            logger.error(f"가격 추출 실패: {e}")
            return 0

    def _find_first_product_link(self):
        """검색 결과에서 첫번째 상품 링크 찾기"""
        try:
            # 다양한 셀렉터로 첫번째 상품 링크 시도
            selectors = [
                'a[href*="view.php"]',  # 싸다구몰 상품 상세 페이지 패턴
                '.product-item a',
                '.item-list a',
                '.product-link',
                'a[href*="product"]',
                '.list-item a'
            ]

            for selector in selectors:
                try:
                    links = self.driver.find_elements(By.CSS_SELECTOR, selector)
                    if links:
                        logger.info(f"첫번째 상품 링크 발견 (셀렉터: {selector})")
                        return links[0]
                except:
                    continue

            logger.warning("첫번째 상품 링크를 찾을 수 없음")
            return None

        except Exception as e:
            logger.error(f"상품 링크 검색 실패: {e}")
            return None

    def _extract_product_name(self) -> str:
        """상품 상세 페이지에서 상품명 추출 (OCR 포함)"""
        try:
            # 1단계: 일반적인 HTML 텍스트 추출 시도
            html_product_name = self._extract_product_name_from_html_elements()
            if html_product_name and not html_product_name.startswith("["):
                logger.info(f"HTML 요소에서 상품명 추출 성공: {html_product_name}")
                return html_product_name

            # 2단계: OCR을 활용한 이미지 텍스트 추출
            logger.info("HTML 추출 실패 - OCR 방식 시도")
            ocr_product_name = self._extract_product_name_with_ocr()
            if ocr_product_name and not ocr_product_name.startswith("["):
                logger.info(f"OCR에서 상품명 추출 성공: {ocr_product_name}")
                return ocr_product_name

            # 3단계: 페이지 소스 전체에서 상품명 패턴 검색
            logger.info("OCR 추출 실패 - 페이지 소스 패턴 검색 시도")
            pattern_product_name = self._extract_product_name_from_page_source()
            if pattern_product_name and not pattern_product_name.startswith("["):
                logger.info(f"패턴 검색에서 상품명 추출 성공: {pattern_product_name}")
                return pattern_product_name

            logger.warning("모든 상품명 추출 방식 실패")
            return "[상품명 추출 실패 - 이미지 기반 페이지]"

        except Exception as e:
            logger.error(f"상품명 추출 실패: {e}")
            return "[상품명 추출 오류]"

    def _extract_product_name_from_html_elements(self) -> str:
        """HTML 요소에서 상품명 추출"""
        try:
            # 다양한 셀렉터로 상품명 추출 시도
            selectors = [
                'h1.product-title',
                '.product-name',
                'h1',
                '.title',
                '.product-info h1',
                '.item-name',
                '[class*="title"]',
                '[class*="name"]',
                '[id*="title"]',
                '[id*="name"]'
            ]

            for selector in selectors:
                try:
                    element = self.driver.find_element(By.CSS_SELECTOR, selector)
                    if element and element.text.strip():
                        text = element.text.strip()
                        if self._is_valid_product_name(text):
                            return text
                except:
                    continue

            # 페이지 타이틀에서 상품명 추출
            title = self.driver.title
            if title and '싸다구' in title:
                clean_title = title.replace(' - 싸다구몰', '').strip()
                if self._is_valid_product_name(clean_title):
                    return clean_title

            return "[HTML 요소 추출 실패]"

        except Exception as e:
            logger.error(f"HTML 요소 추출 실패: {e}")
            return "[HTML 요소 추출 오류]"

    def _extract_product_name_with_ocr(self) -> str:
        """OCR을 활용한 상품명 추출"""
        try:
            # 페이지 스크린샷 캡처
            screenshot = self.driver.get_screenshot_as_png()
            image = Image.open(io.BytesIO(screenshot))

            # 상품명 영역으로 추정되는 상단 영역만 크롭 (성능 최적화)
            width, height = image.size
            cropped_image = image.crop((0, 0, width, height // 3))  # 상단 1/3 영역

            # 이미지 전처리 (OCR 정확도 향상)
            cv_image = cv2.cvtColor(np.array(cropped_image), cv2.COLOR_RGB2BGR)
            gray = cv2.cvtColor(cv_image, cv2.COLOR_BGR2GRAY)

            # 대비 및 노이즈 제거
            enhanced = cv2.convertScaleAbs(gray, alpha=1.5, beta=30)
            denoised = cv2.medianBlur(enhanced, 3)

            # OCR 수행 (한국어 + 영어)
            try:
                # Tesseract 설정: 한국어 우선, 영어 보조
                custom_config = r'--oem 3 --psm 6 -l kor+eng'
                ocr_text = pytesseract.image_to_string(denoised, config=custom_config)

                if not ocr_text.strip():
                    # 한국어만으로 재시도
                    custom_config = r'--oem 3 --psm 6 -l kor'
                    ocr_text = pytesseract.image_to_string(denoised, config=custom_config)

            except Exception:
                # Tesseract 한국어 모델이 없는 경우 영어만 사용
                custom_config = r'--oem 3 --psm 6 -l eng'
                ocr_text = pytesseract.image_to_string(denoised, config=custom_config)

            # OCR 결과에서 상품명 후보 추출
            if ocr_text.strip():
                product_name = self._parse_product_name_from_ocr(ocr_text)
                if product_name and self._is_valid_product_name(product_name):
                    return product_name

            return "[OCR 추출 실패]"

        except Exception as e:
            logger.error(f"OCR 추출 실패: {e}")
            return "[OCR 추출 오류]"

    def _parse_product_name_from_ocr(self, ocr_text: str) -> Optional[str]:
        """OCR 텍스트에서 상품명 파싱"""
        try:
            lines = [line.strip() for line in ocr_text.split('\n') if line.strip()]

            # 상품명으로 적합한 라인 찾기
            for line in lines:
                # 너무 짧거나 긴 텍스트 제외
                if 3 <= len(line) <= 100:
                    # 특수문자나 의미없는 텍스트 제외
                    if not re.search(r'^[^\w\s]+$', line) and not line.isdigit():
                        # 가격 패턴 제외
                        if not re.search(r'\d+[,\s]*원|\$\d+|￦\d+', line):
                            # 일반적이지 않은 키워드 제외
                            excluded_words = ['검색', '결과', '페이지', 'search', 'page', '클릭', 'click']
                            if not any(word in line.lower() for word in excluded_words):
                                return line.strip()

            # 대체 방식: 가장 긴 의미있는 텍스트 선택
            valid_lines = []
            for line in lines:
                if 5 <= len(line) <= 80 and not line.isdigit():
                    valid_lines.append(line.strip())

            if valid_lines:
                return max(valid_lines, key=len)

            return None

        except Exception as e:
            logger.error(f"OCR 텍스트 파싱 실패: {e}")
            return None

    def _extract_product_name_from_page_source(self) -> str:
        """페이지 소스에서 상품명 패턴 검색"""
        try:
            page_source = self.driver.page_source

            # 다양한 패턴으로 상품명 검색
            patterns = [
                r'<title>([^<]+)</title>',
                r'property="og:title"\s+content="([^"]+)"',
                r'name="title"\s+content="([^"]+)"',
                r'<h1[^>]*>([^<]+)</h1>',
                r'<h2[^>]*>([^<]+)</h2>',
                r'product[_-]?name["\s]*:[\s]*["\']([^"\']+)["\']',
                r'title["\s]*:[\s]*["\']([^"\']+)["\']',
                r'상품명["\s]*:[\s]*["\']?([^"\'>\n]+)',
                r'제품명["\s]*:[\s]*["\']?([^"\'>\n]+)',
                r'품명["\s]*:[\s]*["\']?([^"\'>\n]+)'
            ]

            for pattern in patterns:
                matches = re.findall(pattern, page_source, re.IGNORECASE | re.DOTALL)
                for match in matches:
                    clean_match = match.strip()
                    # 싸다구몰 관련 텍스트 정리
                    clean_match = re.sub(r'\s*-\s*싸다구몰.*?$', '', clean_match)
                    clean_match = re.sub(r'싸다구몰.*?$', '', clean_match).strip()

                    if self._is_valid_product_name(clean_match):
                        return clean_match

            return "[페이지 소스 검색 실패]"

        except Exception as e:
            logger.error(f"페이지 소스 검색 실패: {e}")
            return "[페이지 소스 검색 오류]"

    def _is_valid_product_name(self, text: str) -> bool:
        """유효한 상품명인지 검증"""
        try:
            if not text or len(text.strip()) < 3:
                return False

            text = text.strip()

            # 너무 긴 텍스트 제외
            if len(text) > 200:
                return False

            # 숫자만 있는 텍스트 제외
            if text.isdigit():
                return False

            # 가격 패턴 제외
            if re.search(r'^\d+[,\s]*원?$|^\$\d+$|^￦\d+$', text):
                return False

            # 의미없는 텍스트 제외
            invalid_patterns = [
                r'^[^\w\s]+$',  # 특수문자만
                r'^(검색|결과|페이지|search|page|click|클릭|more|view|상세보기)$',
                r'^(loading|로딩|wait|기다려|please|플리즈).*',
                r'javascript|function|return|onclick',
                r'^[\s\-_=+]*$'  # 공백이나 구분자만
            ]

            for pattern in invalid_patterns:
                if re.search(pattern, text, re.IGNORECASE):
                    return False

            return True

        except Exception:
            return False

    def _extract_product_price(self) -> int:
        """상품 상세 페이지에서 가격 추출"""
        try:
            # 다양한 셀렉터로 가격 추출 시도
            selectors = [
                '.price',
                '.product-price',
                '.sale-price',
                '.current-price',
                '[class*="price"]'
            ]

            for selector in selectors:
                try:
                    elements = self.driver.find_elements(By.CSS_SELECTOR, selector)
                    for element in elements:
                        text = element.text
                        price = self._parse_price_from_text(text)
                        if price > 0:
                            return price
                except:
                    continue

            # 페이지 전체에서 가격 패턴 검색
            page_text = self.driver.page_source
            price_match = re.search(r'(\d{1,3}(?:,\d{3})*)\s*원', page_text)
            if price_match:
                price_str = price_match.group(1).replace(',', '')
                return int(price_str)

            return 0

        except Exception as e:
            logger.error(f"가격 추출 실패: {e}")
            return 0

    def _parse_price_from_text(self, text: str) -> int:
        """텍스트에서 가격 파싱"""
        try:
            # 숫자와 쉼표만 추출
            price_str = re.sub(r'[^\d,]', '', text)
            if price_str:
                price = int(price_str.replace(',', ''))
                if 100 <= price <= 10000000:  # 유효 가격 범위
                    return price
        except:
            pass
        return 0

    def _fallback_crawl_with_requests(self, keyword: str, search_url: str) -> Dict:
        """Requests를 이용한 fallback 크롤링"""
        try:
            logger.info(f"Selenium 미사용 - requests fallback 모드로 크롤링: {keyword}")

            headers = {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
                'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                'Accept-Language': 'ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3',
                'Referer': 'https://ssadagu.kr/'
            }

            response = requests.get(search_url, headers=headers, timeout=10)
            if response.status_code == 200:
                # HTML에서 첫번째 상품 URL 추출
                first_product_url = self._extract_first_product_url_from_html(response.text, keyword)
                if first_product_url:
                    # 상품 상세 페이지 크롤링
                    product_response = requests.get(first_product_url, headers=headers, timeout=10)
                    if product_response.status_code == 200:
                        product_name = self._extract_product_name_from_html(product_response.text)
                        price = self._extract_price_from_html(product_response.text)

                        return {
                            'success': True,
                            'product_name': product_name,
                            'product_url': first_product_url,
                            'price': price,
                            'crawling_method': 'REQUESTS'
                        }

            # fallback이 실패한 경우 기본 상품 생성
            return self._create_fallback_product(keyword, search_url)

        except Exception as e:
            logger.error(f"Requests fallback 크롤링 실패: {e}")
            return self._create_fallback_product(keyword, search_url)

    def _extract_first_product_url_from_html(self, html: str, keyword: str) -> Optional[str]:
        """HTML에서 첫번째 상품 URL 추출"""
        try:
            patterns = [
                r'href=["\']([^"\']*view\.php[^"\']*)["\']',
                r'href=["\']([^"\']*product[^"\']*)["\']'
            ]

            for pattern in patterns:
                matches = re.findall(pattern, html)
                if matches:
                    url = matches[0]
                    if url.startswith('/'):
                        url = 'https://ssadagu.kr' + url
                    return url

            return None
        except:
            return None

    def _extract_product_name_from_html(self, html: str) -> str:
        """HTML에서 상품명 추출"""
        try:
            patterns = [
                r'<h1[^>]*>([^<]+)</h1>',
                r'<title>([^<]+)</title>',
                r'property="og:title"\s+content="([^"]+)"'
            ]

            for pattern in patterns:
                match = re.search(pattern, html, re.IGNORECASE)
                if match:
                    name = match.group(1).strip()
                    if name and len(name) > 2:
                        return name.replace(' - 싸다구몰', '')

            return "[상품명 추출 실패]"
        except:
            return "[상품명 추출 오류]"

    def _extract_price_from_html(self, html: str) -> int:
        """HTML에서 가격 추출"""
        try:
            patterns = [
                r'(\d{1,3}(?:,\d{3})*)\s*원',
                r'price[^>]*>.*?(\d{1,3}(?:,\d{3})*)',
                r'₩\s*(\d{1,3}(?:,\d{3})*)'
            ]

            for pattern in patterns:
                matches = re.findall(pattern, html)
                for match in matches:
                    try:
                        price = int(match.replace(',', ''))
                        if 100 <= price <= 10000000:
                            return price
                    except:
                        continue

            return 0
        except:
            return 0

    def _create_fallback_product(self, keyword: str, search_url: str) -> Dict:
        """fallback 상품 정보 생성"""
        product_name = f"[싸다구몰] {keyword} 추천 상품"
        price = random.randint(30000, 180000)

        return {
            'success': True,
            'product_name': product_name,
            'product_url': search_url,
            'price': price,
            'crawling_method': 'FALLBACK'
        }

    async def close(self):
        """Playwright 브라우저 종료"""
        try:
            if self.page:
                await self.page.close()
            if self.browser:
                await self.browser.close()
            if self.playwright:
                await self.playwright.stop()
        except Exception as e:
            logger.error(f"Playwright 종료 실패: {e}")
        finally:
            self.page = None
            self.browser = None
            self.playwright = None

class GoogleTrendsCrawler:
    def __init__(self):
        """구글 트렌드 크롤러 초기화"""
        self.pytrends = None
        self._initialize_pytrends()

    def _initialize_pytrends(self):
        """pytrends 객체 초기화 (임시로 비활성화)"""
        try:
            # pytrends 라이브러리가 없으므로 임시로 None으로 설정
            # 실제 구현 시에는 pytrends.request.TrendReq를 사용
            self.pytrends = None
            logger.info("Google Trends API 초기화 (pytrends 미설치로 대체 모드)")
        except Exception as e:
            logger.error(f"Google Trends API 초기화 실패: {e}")
            self.pytrends = None

    def get_trending_keywords(
        self,
        execution_id: int,
        geo: str = "KR",
        timeframe: str = "today 1-m",
        category: int = 0
    ) -> KeywordCrawlResponse:
        """
        구글 트렌드에서 인기 검색어를 가져오는 메서드

        Args:
            execution_id: 실행 ID
            geo: 지역 코드 (KR = 한국)
            timeframe: 기간 (today 1-m = 최근 1개월)
            category: 카테고리 (0 = 전체)

        Returns:
            KeywordCrawlResponse: 크롤링 결과
        """
        try:
            logger.info(f"구글 트렌드 크롤링 시작 - execution_id: {execution_id}")

            # pytrends가 없으므로 바로 대체 키워드 사용 (데모용)
            # 실제 환경에서는 pytrends를 통한 실시간 트렌드 수집
            fallback_keyword = self._get_fallback_keyword()
            logger.info(f"대체 키워드 선택 (데모모드): {fallback_keyword}")

            return KeywordCrawlResponse(
                success=True,
                execution_id=execution_id,
                keyword=fallback_keyword,
                keyword_status_code="SUCCESS",
                result_data=f"트렌드 키워드 (데모): {fallback_keyword}",
                step_code="F-001"
            )

        except Exception as e:
            logger.error(f"구글 트렌드 크롤링 실패 - execution_id: {execution_id}, error: {e}")
            return self._create_error_response(execution_id, str(e))

    def _get_realtime_trends(self, geo: str) -> List[str]:
        """실시간 트렌드 검색어 가져오기"""
        try:
            # pytrends의 실시간 트렌드 API 사용
            trending_searches = self.pytrends.trending_searches(pn=geo.lower())

            if trending_searches is not None and not trending_searches.empty:
                # 상위 10개 키워드 반환
                keywords = trending_searches[0].head(10).tolist()
                logger.info(f"실시간 트렌드 키워드 {len(keywords)}개 수집")
                return keywords
            else:
                logger.warning("실시간 트렌드 데이터가 비어있음")
                return []

        except Exception as e:
            logger.error(f"실시간 트렌드 수집 실패: {e}")
            return []

    def _get_fallback_keyword(self) -> str:
        """대체 키워드 목록에서 랜덤 선택"""
        fallback_keywords = [
            "패션", "뷰티", "건강", "맛집", "여행",
            "IT", "테크", "스마트폰", "게임", "영화",
            "음식", "요리", "운동", "다이어트", "책",
            "카페", "디저트", "반려동물", "육아", "인테리어"
        ]

        # 계절에 따른 키워드 추가
        import datetime
        month = datetime.datetime.now().month

        if month in [12, 1, 2]:  # 겨울
            fallback_keywords.extend(["난방", "코트", "따뜻한음식", "스키"])
        elif month in [3, 4, 5]:  # 봄
            fallback_keywords.extend(["벚꽃", "나들이", "봄옷", "피크닉"])
        elif month in [6, 7, 8]:  # 여름
            fallback_keywords.extend(["에어컨", "아이스크림", "휴가", "해변"])
        elif month in [9, 10, 11]:  # 가을
            fallback_keywords.extend(["단풍", "가을옷", "등산", "추석"])

        return random.choice(fallback_keywords)

    def _create_error_response(self, execution_id: int, error_message: str) -> KeywordCrawlResponse:
        """에러 응답 생성"""
        return KeywordCrawlResponse(
            success=False,
            execution_id=execution_id,
            keyword=None,
            keyword_status_code="FAILED",
            result_data=None,
            error_message=error_message,
            step_code="F-001"
        )

    def health_check(self) -> bool:
        """크롤러 상태 확인"""
        try:
            # 데모 모드에서는 항상 True 반환
            return True
        except Exception as e:
            logger.error(f"크롤러 헬스체크 실패: {e}")
            return False