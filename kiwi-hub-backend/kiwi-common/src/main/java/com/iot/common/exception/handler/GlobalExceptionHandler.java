package com.iot.common.exception.handler;


import com.iot.common.constant.MessageConstant;
import com.iot.common.exception.UnauthorizedRequestException;
import com.iot.common.exception.OssException;
import com.iot.common.exception.ServiceException;
import com.iot.common.result.Result;
import com.iot.common.result.ResultCodeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;

/**
 * 全局异常处理
 * @author wan
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @PostConstruct
    public void init() {
        log.info("[全局异常处理初始化成功]");
    }

//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(value = Exception.class)
//    public Result<Object> handleException(Exception e) {
//        log.error("[系统异常]: {}", e.getMessage());
//        return Result.result(ResultCodeEnum.SYSTEM_ERROR);
//    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                // 获取注解中的第一个错误信息
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(MessageConstant.REQUEST_PARAM_VALIDATE_FAIL);
        log.error("[参数校验异常]: {}", errorMsg);
        return Result.result(ResultCodeEnum.REQUEST_PARAM_VALIDATE_FAIL).message(errorMsg);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = OssException.class)
    public Result<Object> handleOssException(OssException e) {
        log.error("[OSS异常]: {}", e.getMessage());
        return Result.fail().message(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = FileCountLimitExceededException.class)
    public Result<Object> handleFileCountLimitExceededException(FileCountLimitExceededException e) {
        log.error("[文件数量超出限制异常]: {}", e.getMessage());
        return Result.result(ResultCodeEnum.SYSTEM_ERROR);
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(value = ConnectException.class)
    public Result<Object> handleConnectException(ConnectException e) {
        log.error("[连接异常]: {}", e.getMessage());
        return Result.result(ResultCodeEnum.DEPENDENCY_SERVICE_UNAVAILABLE);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = UnauthorizedRequestException.class)
    public Result<Object> handleNotVerifiedRequestException(UnauthorizedRequestException e) {
        log.error("[请求异常]: {}", e.getMessage());
        return Result.result(ResultCodeEnum.UNAUTHORIZED);
    }

    @ExceptionHandler(value = ServiceException.class)
    public Result<Object> handleServiceException(ServiceException e) {
        log.error("[业务执行异常]: {}", e.getMessage());
        return Result.fail()
                .message(e.getMessage() == null ? MessageConstant.BUSINESS_EXECUTION_FAIL : e.getMessage());
    }

}
