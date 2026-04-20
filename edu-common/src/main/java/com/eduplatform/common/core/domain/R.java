package com.eduplatform.common.core.domain;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应封装
 * <p>
 * 所有 REST 接口返回此对象。code=200 表示成功，其他表示失败。
 * 前端通过 {@code response.data.code} 判断业务状态。
 * </p>
 *
 * @param <T> 响应数据类型
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;
    public static final String SUCCESS_MSG = "操作成功";
    public static final String FAIL_MSG = "操作失败";

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS_CODE, SUCCESS_MSG);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS_CODE, SUCCESS_MSG);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, SUCCESS_CODE, msg);
    }

    public static <T> R<T> fail() {
        return restResult(null, FAIL_CODE, FAIL_MSG);
    }

    public static <T> R<T> fail(String msg) {
        return restResult(null, FAIL_CODE, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    public boolean isSuccess() {
        return SUCCESS_CODE == this.code;
    }
}
