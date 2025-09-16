package com.softlabs.aicontents.domain.datacollector.mapper;

import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import com.softlabs.aicontents.domain.datacollector.vo.request.ProductSearchVo;
import com.softlabs.aicontents.domain.datacollector.vo.response.ProductInfoVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductInfoMapper {

  int insertProduct(ProductInfo productInfo);

  List<ProductInfoVo> selectRecentProducts(@Param("limit") int limit);

  List<ProductInfoVo> selectProductsByCondition(ProductSearchVo searchVo);

  ProductInfoVo selectProductById(@Param("productId") Long productId);

  int countProductsByKeyword(@Param("keyword") String keyword);

  Long selectLastInsertId();
}