package com.softlabs.aicontents.domain.ai.dto.request;

import java.util.List;

/**
 * FastAPI의 요청 JSON과 1:1 매핑
 */

public class ProductBrief {
    public String product_name;
    public String source_url;
    public String price;
    public List<String> keywords;

    public ProductBrief() {}
    public ProductBrief(String product_name, String source_url, String price, List<String> keywords) {
        this.product_name = product_name;
        this.source_url = source_url;
        this.price = price;
        this.keywords = keywords;
    }
}
