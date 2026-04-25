package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.service.SmsService;
import com.rabbiter.oes.util.ApiResultHandler;
import com.rabbiter.oes.util.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    // 短信验证码存储（生产环境应使用 Redis）
    private static final ConcurrentHashMap<String, SmsCode> smsCodeCache = new ConcurrentHashMap<>();

    // 短信验证码有效期（5 分钟）
    private static final long SMS_CODE_EXPIRE = 5 * 60 * 1000;

    @Autowired
    @Qualifier("mockSmsService")
    private SmsService smsService;

    /**
     * 获取图形验证码
     */
    @GetMapping("/image")
    public ApiResult getImageCaptcha(HttpSession session) {
        try {
            CaptchaUtil.CaptchaResult captcha = CaptchaUtil.generateCaptcha();

            // 将验证码存入session
            session.setAttribute("captcha_code", captcha.getCode());
            session.setAttribute("captcha_time", System.currentTimeMillis());

            Map<String, String> result = new HashMap<>();
            result.put("image", captcha.getImageBase64());

            return ApiResultHandler.buildApiResult(200, "获取成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "验证码生成失败: " + e.getMessage(), null);
        }
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public ApiResult sendSmsCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");

        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return ApiResultHandler.buildApiResult(400, "手机号格式不正确", null);
        }

        // 检查是否在60秒内重复发送
        SmsCode existing = smsCodeCache.get(phone);
        if (existing != null && System.currentTimeMillis() - existing.createTime < 60000) {
            return ApiResultHandler.buildApiResult(400, "请60秒后再试", null);
        }

        // 生成6位随机验证码
        String code = generateSmsCode();

        // 存储验证码
        smsCodeCache.put(phone, new SmsCode(code, System.currentTimeMillis()));
        
        // 使用短信服务发送验证码
        boolean sendResult = smsService.sendSmsCode(phone, code);
                
        if (!sendResult) {
            log.error("短信发送失败，手机号：{}", phone);
            return ApiResultHandler.buildApiResult(500, "短信发送失败", null);
        }
        
        System.out.println("========================================");
        System.out.println("【短信验证码】手机号：" + phone + ", 验证码：" + code);
        System.out.println("========================================");

        // 返回成功（不在响应中返回验证码）
        Map<String, Object> result = new HashMap<>();
        result.put("message", "验证码已发送");

        return ApiResultHandler.buildApiResult(200, "发送成功", result);
    }

    /**
     * 校验短信验证码（内部调用）
     */
    public static boolean verifySmsCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }

        SmsCode smsCode = smsCodeCache.get(phone);
        if (smsCode == null) {
            return false;
        }

        // 检查是否过期
        if (System.currentTimeMillis() - smsCode.createTime > SMS_CODE_EXPIRE) {
            smsCodeCache.remove(phone);
            return false;
        }

        // 校验验证码
        if (smsCode.code.equals(code)) {
            smsCodeCache.remove(phone); // 验证成功后删除
            return true;
        }

        return false;
    }

    /**
     * 校验图形验证码（内部调用）
     */
    public static boolean verifyImageCaptcha(HttpSession session, String input) {
        String storedCode = (String) session.getAttribute("captcha_code");
        Long createTime = (Long) session.getAttribute("captcha_time");

        if (storedCode == null || createTime == null) {
            return false;
        }

        // 检查是否过期（2分钟）
        if (System.currentTimeMillis() - createTime > 2 * 60 * 1000) {
            session.removeAttribute("captcha_code");
            session.removeAttribute("captcha_time");
            return false;
        }

        // 校验验证码
        boolean result = CaptchaUtil.verify(input, storedCode);
        if (result) {
            session.removeAttribute("captcha_code");
            session.removeAttribute("captcha_time");
        }

        return result;
    }

    /**
     * 生成6位短信验证码
     */
    private String generateSmsCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 短信验证码存储对象
     */
    private static class SmsCode {
        String code;
        long createTime;

        SmsCode(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
    }
}
