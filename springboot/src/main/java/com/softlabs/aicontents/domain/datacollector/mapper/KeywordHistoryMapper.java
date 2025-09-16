package com.softlabs.aicontents.domain.datacollector.mapper;

import com.softlabs.aicontents.domain.datacollector.vo.response.KeywordHistoryVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KeywordHistoryMapper {

  int insertUsedKeyword(@Param("productId") Long productId,
                        @Param("keyword") String keyword,
                        @Param("source") String source);

  boolean isKeywordUsedRecently(@Param("keyword") String keyword,
                                @Param("days") int days);

  List<KeywordHistoryVo> selectRecentKeywords(@Param("limit") int limit);

  List<KeywordHistoryVo> selectKeywordsByProductId(@Param("productId") Long productId);

  int countKeywordUsage(@Param("keyword") String keyword,
                        @Param("days") int days);
}