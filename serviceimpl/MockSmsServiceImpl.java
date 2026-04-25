package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务实现类（模拟实现）
 * 
 * TODO: 生产环境请替换为真实的短信服务（阿里云、腾讯云等）
 * 
 * @author OES
 * @date 2026-03-29
 */
@Slf4j
@Service("mockSmsService")
public class MockSmsServiceImpl implements SmsService {

    /**
     * 模拟发送短信验证码
     * 实际项目中应接入阿里云、腾讯云等短信服务
     */
    @Override
    public boolean sendSmsCode(String phone, String code) {
        log.info("【模拟短信服务】向手机号 {} 发送验证码：{}", phone, code);
        
        // 模拟发送延迟
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error("发送短信中断", e);
            return false;
        }
        
        log.info("【模拟短信服务】发送成功");
        return true;
    }

    /**
     * 模拟发送普通短信
     */
    @Override
    public boolean sendSms(String phone, String message) {
        log.info("【模拟短信服务】向手机号 {} 发送短信：{}", phone, message);
        
        // 模拟发送延迟
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error("发送短信中断", e);
            return false;
        }
        
        log.info("【模拟短信服务】发送成功");
        return true;
    }
}
