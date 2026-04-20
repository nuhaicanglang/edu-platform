package com.eduplatform.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> records;

    /** 总数 */
    private long total;

    /** 当前页 */
    private int pageNum;

    /** 每页大小 */
    private int pageSize;

    public PageResult() {}

    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}
