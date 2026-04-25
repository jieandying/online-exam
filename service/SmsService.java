package com.rabbiter.oes.service;

/**
 * 短信服务接口
 * 
 * @author OES
 * @date 2026-03-29
 */
public interface SmsService {
    
    /**
     * 发送短信验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 发送结果 true-成功，false-失败
     */
    boolean sendSmsCode(String phone, String code);
    
    /**
     * 发送短信（通用方法）
     * 
     * @param phone 手机号
     * @param message 短信内容
     * @return 发送结果 true-成功，false-失败
     */
    boolean sendSms(String phone, String message);
}
