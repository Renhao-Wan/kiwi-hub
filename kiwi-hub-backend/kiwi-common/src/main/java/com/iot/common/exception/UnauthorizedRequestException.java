package com.iot.common.exception;

/**
 * 不合法的请求
 * @author wan
 */
public class UnauthorizedRequestException extends Exception{
    public UnauthorizedRequestException(String message) {
        super(message);
    }

    public UnauthorizedRequestException() {
        super();
    }
}
