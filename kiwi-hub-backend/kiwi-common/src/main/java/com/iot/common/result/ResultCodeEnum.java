package com.iot.common.result;

import com.iot.common.constant.MessageConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回结果状态信息类
 * @author wan
 */
@Getter
@AllArgsConstructor
public enum ResultCodeEnum {
    // 成功
    SUCCESS(20000, MessageConstant.SUCCESS),
    // 失败
    BUSINESS_EXECUTION_FAIL(40001, MessageConstant.BUSINESS_EXECUTION_FAIL),
    // 请求参数校验失败
    REQUEST_PARAM_VALIDATE_FAIL(40002, MessageConstant.REQUEST_PARAM_VALIDATE_FAIL),
    // 未登录或Session失效
    UNAUTHORIZED(40101, MessageConstant.UNAUTHORIZED),
    // 无权限访问资源
    FORBIDDEN(40301, MessageConstant.FORBIDDEN),
    // 资源不存在
    RESOURCE_NOT_FOUND(40401, MessageConstant.RESOURCE_NOT_FOUND),
    // 业务冲突
    BUSINESS_CONFLICT(40901, MessageConstant.BUSINESS_CONFLICT),
    // 服务器内部错误
    SYSTEM_ERROR(50001, MessageConstant.SYSTEM_ERROR),
    // 依赖服务不可用
    DEPENDENCY_SERVICE_UNAVAILABLE(50301, MessageConstant.DEPENDENCY_SERVICE_UNAVAILABLE);

    private final Integer code;
    private final String message;
}
