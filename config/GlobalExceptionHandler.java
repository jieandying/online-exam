package com.rabbiter.oes.config;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.util.ApiResultHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author OES
 * @date 2026-03-29
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（@RequestBody + @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数校验失败 [{}]: {}", request.getRequestURI(), errorMsg);
        return ApiResultHandler.buildApiResult(400, "参数校验失败：" + errorMsg, null);
    }

    /**
     * 处理绑定异常（表单提交）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleBindException(BindException e, HttpServletRequest request) {
        
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数绑定失败 [{}]: {}", request.getRequestURI(), errorMsg);
        return ApiResultHandler.buildApiResult(400, "参数绑定失败：" + errorMsg, null);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        
        log.warn("缺少请求参数 [{}]: {}", request.getRequestURI(), e.getParameterName());
        return ApiResultHandler.buildApiResult(400, 
                "缺少必要参数：" + e.getParameterName() + " (类型：" + e.getParameterType() + ")", null);
    }

    /**
     * 处理缺少路径变量异常
     */
    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleMissingPathVariableException(
            MissingPathVariableException e, HttpServletRequest request) {
        
        log.warn("缺少路径变量 [{}]: {}", request.getRequestURI(), e.getVariableName());
        return ApiResultHandler.buildApiResult(400, 
                "缺少路径变量：" + e.getVariableName(), null);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        log.warn("参数类型不匹配 [{}]: {} - {}", request.getRequestURI(), 
                e.getName(), e.getMessage());
        return ApiResultHandler.buildApiResult(400, 
                "参数类型错误：" + e.getName() + "，应为 " + e.getRequiredType().getSimpleName(), null);
    }

    /**
     * 处理约束违反异常（@RequestParam + @Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        
        String errorMsg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("约束违反 [{}]: {}", request.getRequestURI(), errorMsg);
        return ApiResultHandler.buildApiResult(400, "参数验证失败：" + errorMsg, null);
    }

    /**
     * 处理数据库唯一约束冲突
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResult handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException e, HttpServletRequest request) {
        
        log.error("数据库约束冲突 [{}]: {}", request.getRequestURI(), e.getMessage());
        
        if (e.getMessage().contains("Duplicate entry")) {
            return ApiResultHandler.buildApiResult(409, "数据已存在，请勿重复添加", null);
        }
        
        return ApiResultHandler.buildApiResult(409, "数据库操作失败：数据完整性约束冲突", null);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        
        log.warn("业务参数异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResultHandler.buildApiResult(400, e.getMessage(), null);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult handleException(
            Exception e, HttpServletRequest request) {
        
        log.error("系统内部异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResultHandler.buildApiResult(500, 
                "系统内部错误：" + e.getClass().getSimpleName() + " - " + e.getMessage(), null);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult handleRuntimeException(
            RuntimeException e, HttpServletRequest request) {
        
        log.error("运行时异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return ApiResultHandler.buildApiResult(500, 
                "运行时错误：" + e.getMessage(), null);
    }
}
