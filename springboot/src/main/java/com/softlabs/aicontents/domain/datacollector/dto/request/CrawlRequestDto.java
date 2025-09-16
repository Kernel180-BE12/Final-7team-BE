package com.softlabs.aicontents.domain.datacollector.dto.request;

import com.softlabs.aicontents.domain.datacollector.model.CrawlJobType;
import lombok.Data;

/**
 * 크롤링 요청 DTO
 * 스케줄러에서 복잡한 크롤링 요청 시 사용
 */
@Data
public class CrawlRequestDto {

    /**
     * 크롤링 타입 (AUTOMATIC 또는 MANUAL)
     */
    private String crawlType;

    /**
     * 크롤링 키워드 (MANUAL 타입에서 필수)
     */
    private String keyword;

    /**
     * 요청자 정보 (선택사항)
     */
    private String requestedBy;

    /**
     * 문자열 크롤링 타입을 ENUM으로 변환
     * @return CrawlJobType ENUM
     * @throws IllegalArgumentException 유효하지 않은 타입인 경우
     */
    public CrawlJobType getCrawlJobType() {
        if (crawlType == null || crawlType.trim().isEmpty()) {
            throw new IllegalArgumentException("크롤링 타입이 지정되지 않았습니다.");
        }
        return CrawlJobType.valueOf(crawlType.toUpperCase());
    }

    /**
     * MANUAL 타입인지 확인
     * @return MANUAL 타입이면 true
     */
    public boolean isManualType() {
        try {
            return getCrawlJobType() == CrawlJobType.MANUAL;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * AUTOMATIC 타입인지 확인
     * @return AUTOMATIC 타입이면 true
     */
    public boolean isAutomaticType() {
        try {
            return getCrawlJobType() == CrawlJobType.AUTOMATIC;
        } catch (Exception e) {
            return false;
        }
    }
}