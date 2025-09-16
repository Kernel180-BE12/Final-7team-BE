package com.softlabs.aicontents.domain.datacollector.dto.response;

import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import lombok.Data;

/**
 * 크롤링 응답 DTO
 * 스케줄러로 반환되는 크롤링 결과
 */
@Data
public class CrawlResponseDto {

    /**
     * 크롤링 성공 여부
     */
    private boolean success;

    /**
     * 크롤링된 상품 정보
     */
    private ProductInfo productInfo;

    /**
     * 응답 메시지
     */
    private String message;

    /**
     * 크롤링 타입
     */
    private String crawlType;

    /**
     * 크롤링 키워드
     */
    private String keyword;

    /**
     * 처리 시간 (밀리초)
     */
    private long processingTimeMs;

    /**
     * 성공 응답 생성
     * @param productInfo 크롤링된 상품 정보
     * @param processingTime 처리 시간
     * @return 성공 응답 DTO
     */
    public static CrawlResponseDto success(ProductInfo productInfo, long processingTime) {
        CrawlResponseDto response = new CrawlResponseDto();
        response.setSuccess(true);
        response.setProductInfo(productInfo);
        response.setMessage("크롤링이 성공적으로 완료되었습니다.");
        response.setCrawlType(productInfo != null ? productInfo.getCrawlType() : null);
        response.setKeyword(productInfo != null ? productInfo.getKeyword() : null);
        response.setProcessingTimeMs(processingTime);
        return response;
    }

    /**
     * 부분 실패 응답 생성
     * @param productInfo 부분적으로 수집된 정보
     * @param processingTime 처리 시간
     * @return 부분 실패 응답 DTO
     */
    public static CrawlResponseDto partialSuccess(ProductInfo productInfo, long processingTime) {
        CrawlResponseDto response = new CrawlResponseDto();
        response.setSuccess(false);
        response.setProductInfo(productInfo);
        response.setMessage("크롤링이 부분적으로 완료되었습니다. 일부 정보가 누락될 수 있습니다.");
        response.setCrawlType(productInfo != null ? productInfo.getCrawlType() : null);
        response.setKeyword(productInfo != null ? productInfo.getKeyword() : null);
        response.setProcessingTimeMs(processingTime);
        return response;
    }

    /**
     * 실패 응답 생성
     * @param errorMessage 오류 메시지
     * @param crawlType 크롤링 타입
     * @param keyword 크롤링 키워드
     * @param processingTime 처리 시간
     * @return 실패 응답 DTO
     */
    public static CrawlResponseDto failure(String errorMessage, String crawlType, String keyword, long processingTime) {
        CrawlResponseDto response = new CrawlResponseDto();
        response.setSuccess(false);
        response.setProductInfo(null);
        response.setMessage(errorMessage);
        response.setCrawlType(crawlType);
        response.setKeyword(keyword);
        response.setProcessingTimeMs(processingTime);
        return response;
    }
}