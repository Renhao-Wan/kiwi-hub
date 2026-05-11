package com.iot.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 统一响应结果
 * @author wan
 */
@Schema(description = "统一响应结果")
@SuppressWarnings("unused")
@Getter
@Setter
@NoArgsConstructor(staticName = "of")
public class Result<T> {
    @Schema(description = "响应状态码", example = "20000")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public static <T> Result<T> success() {
        return Result.<T>of()
                .message(ResultCodeEnum.SUCCESS.getMessage())
                .code(ResultCodeEnum.SUCCESS.getCode());
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>of()
                .message(ResultCodeEnum.SUCCESS.getMessage())
                .code(ResultCodeEnum.SUCCESS.getCode())
                .data(data);
    }

    public static <T> Result<T> fail() {
        return Result.<T>of()
                .message(ResultCodeEnum.BUSINESS_EXECUTION_FAIL.getMessage())
                .code(ResultCodeEnum.BUSINESS_EXECUTION_FAIL.getCode());
    }

    public static <T> Result<T> fail(T data) {
        return Result.<T>of()
                .message(ResultCodeEnum.BUSINESS_EXECUTION_FAIL.getMessage())
                .code(ResultCodeEnum.BUSINESS_EXECUTION_FAIL.getCode())
                .data(data);
    }

    public static <T> Result<T> result(ResultCodeEnum resultCodeEnum) {
        return Result.<T>of()
                .message(resultCodeEnum.getMessage())
                .code(resultCodeEnum.getCode());
    }

    public static <T> Result<T> result(ResultCodeEnum resultCodeEnum, T data) {
        return Result.<T>of()
                .message(resultCodeEnum.getMessage())
                .code(resultCodeEnum.getCode())
                .data(data);
    }

    public boolean isSuccess() {
        return this.code.equals(ResultCodeEnum.SUCCESS.getCode());
    }

    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }

    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }
}
