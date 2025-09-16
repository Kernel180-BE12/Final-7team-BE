package com.softlabs.aicontents.domain.scheduler.vo.request;

import lombok.Data;

@Data
public class PagingVO {

    private int startRow;
    private int endRow;

    public PagingVO(int pageNumber, int pageSize) {
        this.startRow = (pageNumber - 1) * pageSize;
        this.endRow = pageNumber * pageSize;
    }
}
