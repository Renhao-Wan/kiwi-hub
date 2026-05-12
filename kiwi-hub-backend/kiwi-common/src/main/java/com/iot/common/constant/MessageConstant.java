package com.iot.common.constant;

/**
 * 响应结果
 * @author wan
 */
public class MessageConstant {
    // 成功
    public static final String SUCCESS = "成功";
    // 业务执行失败（例如转账余额不足、登录密码错误等）
    public static final String BUSINESS_EXECUTION_FAIL = "失败";
    // 请求参数校验失败
    public static final String REQUEST_PARAM_VALIDATE_FAIL = "请求参数校验失败";
    // 未登录或Session失效
    public static final String UNAUTHORIZED = "未登录或Session失效";
    // 无权限访问资源
    public static final String FORBIDDEN = "无权限访问资源";
    // 资源不存在（如文章或短链）
    public static final String RESOURCE_NOT_FOUND = "资源不存在";
    // 业务冲突（如重复点赞）
    public static final String BUSINESS_CONFLICT = "业务冲突";
    // 服务器内部错误
    public static final String SYSTEM_ERROR = "服务器内部错误";
    // 依赖服务不可用（如MongoDB/Redis故障）
    public static final String DEPENDENCY_SERVICE_UNAVAILABLE = "依赖服务不可用";
}
