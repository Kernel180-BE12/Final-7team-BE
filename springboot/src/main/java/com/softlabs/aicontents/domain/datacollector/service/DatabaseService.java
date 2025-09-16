package com.softlabs.aicontents.domain.datacollector.service;

import com.softlabs.aicontents.domain.datacollector.mapper.KeywordHistoryMapper;
import com.softlabs.aicontents.domain.datacollector.mapper.ProductInfoMapper;
import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import com.softlabs.aicontents.domain.datacollector.vo.response.KeywordHistoryVo;
import com.softlabs.aicontents.domain.datacollector.vo.response.ProductInfoVo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseService {

  private final ProductInfoMapper productInfoMapper;
  private final KeywordHistoryMapper keywordHistoryMapper;

  @Transactional
  public void saveProductInfo(ProductInfo productInfo) {
    if (productInfo == null) {
      log.error("ProductInfo가 null입니다.");
      return;
    }

    log.info("ProductInfo 저장 시도 - keyword: {}, product: {}",
             productInfo.getKeyword(), productInfo.getProductName());

    try {
      // 1. 상품 정보 저장
      int result = productInfoMapper.insertProduct(productInfo);
      if (result <= 0) {
        throw new RuntimeException("상품 저장 실패");
      }

      // 2. 마지막 삽입된 ID 조회
      Long productId = productInfoMapper.selectLastInsertId();
      if (productId == null) {
        throw new RuntimeException("상품 ID 생성 실패");
      }

      // 3. 키워드 이력 저장
      int keywordResult = keywordHistoryMapper.insertUsedKeyword(
          productId,
          productInfo.getKeyword(),
          "NAVER_SHOPPING_BEST"
      );

      if (keywordResult <= 0) {
        throw new RuntimeException("키워드 저장 실패");
      }

      log.info("모든 데이터가 성공적으로 저장되었습니다. Product ID: {}", productId);

    } catch (Exception e) {
      log.error("데이터베이스 저장 중 오류 발생", e);
      throw new RuntimeException("데이터베이스 저장 실패: " + e.getMessage(), e);
    }
  }

  public List<ProductInfoVo> getRecentProducts(int limit) {
    try {
      return productInfoMapper.selectRecentProducts(limit);
    } catch (Exception e) {
      log.error("최근 상품 조회 중 오류 발생", e);
      throw new RuntimeException("상품 조회 실패: " + e.getMessage(), e);
    }
  }

  public boolean isKeywordUsedRecently(String keyword) {
    return isKeywordUsedRecently(keyword, 30);
  }

  public boolean isKeywordUsedRecently(String keyword, int days) {
    try {
      return keywordHistoryMapper.isKeywordUsedRecently(keyword, days);
    } catch (Exception e) {
      log.error("키워드 중복 검사 중 오류 발생", e);
      return false; // 에러 발생시 false 반환하여 크롤링 진행
    }
  }

  public List<KeywordHistoryVo> getRecentKeywords(int limit) {
    try {
      return keywordHistoryMapper.selectRecentKeywords(limit);
    } catch (Exception e) {
      log.error("최근 키워드 조회 중 오류 발생", e);
      throw new RuntimeException("키워드 조회 실패: " + e.getMessage(), e);
    }
  }

  public ProductInfoVo getProductById(Long productId) {
    try {
      return productInfoMapper.selectProductById(productId);
    } catch (Exception e) {
      log.error("상품 조회 중 오류 발생 - productId: {}", productId, e);
      throw new RuntimeException("상품 조회 실패: " + e.getMessage(), e);
    }
  }

  public int getKeywordUsageCount(String keyword, int days) {
    try {
      return keywordHistoryMapper.countKeywordUsage(keyword, days);
    } catch (Exception e) {
      log.error("키워드 사용 횟수 조회 중 오류 발생", e);
      return 0;
    }
  }
}