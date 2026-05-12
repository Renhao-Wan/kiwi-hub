package com.iot.common.exception;

/**
 * 对象存储服务异常
 * @author wan
 */
public class OssException extends RuntimeException{

    public OssException() {
        super();
    }

    public OssException(String message) {
        super(message);
    }
    public OssException(String message, Throwable cause) {
        super(message, cause);
    }
}
