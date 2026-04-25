package com.rabbiter.oes.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.rabbiter.oes.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云短信服务实现类
 * 
 * 使用说明：
 * 1. 在 application.properties 中配置相关参数
 * 2. 添加阿里云 SDK 依赖到 pom.xml
 * 3. 修改 @Profile 注解为 "prod" 以在生产环境启用
 * 
 * @author OES
 * @date 2026-03-29
 */
@Slf4j
@Service("aliyunSmsService")
@Profile("dev") // 开发环境使用，生产环境改为 @Profile("prod")
public class AliyunSmsServiceImpl implements SmsService {

    // TODO: 在 application.properties 中添加以下配置
    // aliyun.sms.accessKeyId=your_access_key_id
    // aliyun.sms.accessKeySecret=your_access_key_secret
    // aliyun.sms.signName=你的签名名称
    // aliyun.sms.templateCode=你的模板代码
    
    @Value("${aliyun.sms.accessKeyId:}")
    private String accessKeyId;
    
    @Value("${aliyun.sms.accessKeySecret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.sms.signName:在线考试系统}")
    private String signName;
    
    @Value("${aliyun.sms.templateCode:SMS_123456789}")
    private String templateCode;

    /**
     * 发送短信验证码（阿里云）
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return 发送结果
     */
    @Override
    public boolean sendSmsCode(String phone, String code) {
        try {
            log.info("【阿里云短信】准备发送验证码到：{}", phone);
            
            // TODO: 引入阿里云 SDK
            // 示例代码（需要添加 com.aliyun:aliyun-java-sdk-core 依赖）
            /*
            IClientProfile profile = DefaultProfile.getProfile(
                "cn-hangzhou", accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);
            
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\"" + code + "\"}");
            
            SendSmsResponse response = client.getAcsResponse(request);
            
            if ("OK".equals(response.getCode())) {
                log.info("【阿里云短信】发送成功：{}", phone);
                return true;
            } else {
                log.error("【阿里云短信】发送失败：{} - {}", response.getCode(), response.getMessage());
                return false;
            }
            */
            
            // 临时使用模拟服务
            return sendMockSms(phone, "您的验证码是：" + code + "，5 分钟内有效");
            
        } catch (Exception e) {
            log.error("【阿里云短信】发送异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送普通短信（阿里云）
     */
    @Override
    public boolean sendSms(String phone, String message) {
        try {
            log.info("【阿里云短信】准备发送短信到：{}, 内容：{}", phone, message);
            
            // TODO: 实现阿里云短信发送逻辑
            // 参考 sendSmsCode 方法
            
            return sendMockSms(phone, message);
            
        } catch (Exception e) {
            log.error("【阿里云短信】发送异常：{}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 模拟发送（用于测试）
     */
    private boolean sendMockSms(String phone, String message) {
        log.info("【模拟发送】手机号：{}, 内容：{}", phone, message);
        return true;
    }
}
