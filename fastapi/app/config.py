# publish config
import os

BLOG_ID = os.getenv("NAVER_BLOG_ID")
CATEGORY_NO = os.getenv("NAVER_CATEGORY_NO")
HEADLESS = os.getenv("HEADLESS", "true").lower() == "true"

STATE_PATH = "storage/naver_state.json"

WRITE_URL = f"https://blog.naver.com/{BLOG_ID}/postwrite"

# --- 셀렉터 후보들 ---
# 제목: 꼭 '문서 최상단(메인 문서)'에서 찾는다. (본문은 iframe 내부이므로 분리)
# 제목 입력란 (클릭 후 타이핑할 컨테이너)
TITLE_SELECTOR = "div.se-title-text"

# 본문 편집기 아이프레임(iframe) (title 속성 기반으로 안정성 높임)
IFRAME_SELECTOR = 'iframe[title*="Smart Editor"], iframe[title*="스마트 에디터"]'

# 아이프레임 내부의 본문 입력란 (클릭 후 타이핑할 컨테이너)
BODY_SELECTOR = "body[contenteditable='true']"

# 1차 발행 버튼 (에디터 우측 상단)
EDITOR_PUBLISH_BUTTON_SELECTOR = 'button[data-click-area="tpb.publish"]'

# 발행 팝업(모달) 창의 태그 입력란 (id 기반으로 가장 정확함)
MODAL_TAGS_SELECTOR = '#tag-input'

# 2차 최종 발행 버튼 (팝업 우측 상단) (data-testid 기반으로 가장 안정적)
MODAL_PUBLISH_BUTTON_SELECTOR = 'button[data-testid="seOnePublishBtn"]'