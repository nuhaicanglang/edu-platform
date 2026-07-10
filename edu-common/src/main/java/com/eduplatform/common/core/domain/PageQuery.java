package com.eduplatform.common.core.domain;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    /** 当前页码 */
    @Min(value = 1, message = "pageNum 必须大于等于 1")
    private Integer pageNum = 1;

    /** 每页数量 */
    @Min(value = 1, message = "pageSize 必须大于等于 1")
    @Max(value = 100, message = "pageSize 不能超过 100")
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderByColumn;

    /** 排序方向 asc/desc */
    private String isAsc = "asc";
}
