package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 忘记密码Controller
 */
@RestController
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    // 存储验证码 (生产环境应使用Redis)
    private static final ConcurrentHashMap<String, VerifyCodeInfo> codeCache = new ConcurrentHashMap<>();

    /**
     * 发送重置密码验证码
     * 通过学号/工号和注册邮箱验证身份
     */
    @PostMapping("/sendCode")
    public ApiResult sendCode(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String email = params.get("email");
        String userType = params.get("userType"); // student 或 teacher
        
        if (userId == null || email == null || userType == null) {
            return ApiResultHandler.buildApiResult(400, "参数不完整", null);
        }

        // 验证用户是否存在
        boolean userExists = false;
        if ("student".equals(userType)) {
            Student student = studentMapper.findById(Integer.parseInt(userId));
            if (student != null && email.equals(student.getEmail())) {
                userExists = true;
            }
        } else if ("teacher".equals(userType)) {
            Teacher teacher = teacherMapper.findById(Integer.parseInt(userId));
            if (teacher != null && email.equals(teacher.getEmail())) {
                userExists = true;
            }
        }

        if (!userExists) {
            return ApiResultHandler.buildApiResult(400, "用户信息不匹配", null);
        }

        // 生成6位验证码
        String code = generateCode();
        String cacheKey = userType + "_" + userId;
        
        // 存储验证码（5分钟有效）
        codeCache.put(cacheKey, new VerifyCodeInfo(code, System.currentTimeMillis() + 5 * 60 * 1000, email));

        // 实际应发送邮件，这里模拟（开发环境）
        Map<String, Object> result = new HashMap<>();
        result.put("message", "验证码已发送到邮箱");
        // 注意：生产环境绝对不要在响应中返回验证码
        
        return ApiResultHandler.buildApiResult(200, "发送成功", result);
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verifyCode")
    public ApiResult verifyCode(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String code = params.get("code");
        String userType = params.get("userType");

        if (userId == null || code == null || userType == null) {
            return ApiResultHandler.buildApiResult(400, "参数不完整", null);
        }

        String cacheKey = userType + "_" + userId;
        VerifyCodeInfo info = codeCache.get(cacheKey);

        if (info == null) {
            return ApiResultHandler.buildApiResult(400, "验证码不存在或已过期", null);
        }

        if (System.currentTimeMillis() > info.expireTime) {
            codeCache.remove(cacheKey);
            return ApiResultHandler.buildApiResult(400, "验证码已过期", null);
        }

        if (!code.equals(info.code)) {
            return ApiResultHandler.buildApiResult(400, "验证码错误", null);
        }

        // 验证成功，标记可以重置密码
        info.verified = true;
        return ApiResultHandler.buildApiResult(200, "验证成功", null);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset")
    public ApiResult resetPassword(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String newPassword = params.get("newPassword");
        String userType = params.get("userType");

        if (userId == null || newPassword == null || userType == null) {
            return ApiResultHandler.buildApiResult(400, "参数不完整", null);
        }

        if (newPassword.length() < 6) {
            return ApiResultHandler.buildApiResult(400, "密码长度不能少于6位", null);
        }

        String cacheKey = userType + "_" + userId;
        VerifyCodeInfo info = codeCache.get(cacheKey);

        if (info == null || !info.verified) {
            return ApiResultHandler.buildApiResult(400, "请先完成验证", null);
        }

        // 重置密码
        int result = 0;
        if ("student".equals(userType)) {
            result = studentMapper.updatePassword(Integer.parseInt(userId), newPassword);
        } else if ("teacher".equals(userType)) {
            result = teacherMapper.updatePassword(Integer.parseInt(userId), newPassword);
        }

        if (result > 0) {
            codeCache.remove(cacheKey);
            return ApiResultHandler.buildApiResult(200, "密码重置成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "密码重置失败", null);
    }

    /**
     * 通过安全问题重置密码（备用方案）
     */
    @PostMapping("/resetByQuestion")
    public ApiResult resetByQuestion(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String userType = params.get("userType");
        String newPassword = params.get("newPassword");
        String cardId = params.get("cardId"); // 使用身份证号后6位作为安全验证

        if (userId == null || userType == null || newPassword == null || cardId == null) {
            return ApiResultHandler.buildApiResult(400, "参数不完整", null);
        }

        if (newPassword.length() < 6) {
            return ApiResultHandler.buildApiResult(400, "密码长度不能少于6位", null);
        }

        // 验证身份证号
        boolean verified = false;
        if ("student".equals(userType)) {
            Student student = studentMapper.findById(Integer.parseInt(userId));
            if (student != null && student.getCardId() != null) {
                String last6 = student.getCardId().length() >= 6 ? 
                    student.getCardId().substring(student.getCardId().length() - 6) : student.getCardId();
                verified = cardId.equals(last6);
            }
        } else if ("teacher".equals(userType)) {
            Teacher teacher = teacherMapper.findById(Integer.parseInt(userId));
            if (teacher != null && teacher.getCardId() != null) {
                String last6 = teacher.getCardId().length() >= 6 ? 
                    teacher.getCardId().substring(teacher.getCardId().length() - 6) : teacher.getCardId();
                verified = cardId.equals(last6);
            }
        }

        if (!verified) {
            return ApiResultHandler.buildApiResult(400, "身份验证失败", null);
        }

        // 重置密码
        int result = 0;
        if ("student".equals(userType)) {
            result = studentMapper.updatePassword(Integer.parseInt(userId), newPassword);
        } else if ("teacher".equals(userType)) {
            result = teacherMapper.updatePassword(Integer.parseInt(userId), newPassword);
        }

        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "密码重置成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "密码重置失败", null);
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // 验证码信息内部类
    private static class VerifyCodeInfo {
        String code;
        long expireTime;
        String email;
        boolean verified = false;

        VerifyCodeInfo(String code, long expireTime, String email) {
            this.code = code;
            this.expireTime = expireTime;
            this.email = email;
        }
    }
}
