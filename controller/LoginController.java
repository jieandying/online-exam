package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.LoginMapper;
import com.rabbiter.oes.serviceimpl.LoginServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@RestController
public class LoginController {

    @Autowired
    private LoginServiceImpl loginService;
    
    @Autowired
    private LoginMapper loginMapper;

    @PostMapping("/login")
    public ApiResult login(@RequestBody Login login, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String loginType = login.getLoginType();
        
        // 短信验证码登录
        if ("sms".equals(loginType)) {
            return smsLogin(login, response);
        }
        
        // 密码登录 - 需要验证图形验证码
        String captcha = login.getCaptcha();
        if (captcha == null || captcha.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入验证码", null);
        }
        
        if (!CaptchaController.verifyImageCaptcha(session, captcha)) {
            return ApiResultHandler.buildApiResult(400, "验证码错误或已过期", null);
        }

        Integer username = login.getUsername();
        String password = login.getPassword();
        Admin adminRes = loginService.adminLogin(username, password);
        if (adminRes != null) {
            setCookies(response, adminRes.getCardId(), "0");
            return ApiResultHandler.buildApiResult(200, "登录成功", adminRes);
        }

        Teacher teacherRes = loginService.teacherLogin(username, password);
        if (teacherRes != null) {
            setCookies(response, teacherRes.getCardId(), "1");
            return ApiResultHandler.buildApiResult(200, "登录成功", teacherRes);
        }

        Student studentRes = loginService.studentLogin(username, password);
        if (studentRes != null) {
            setCookies(response, studentRes.getCardId(), "2");
            return ApiResultHandler.buildApiResult(200, "登录成功", studentRes);
        }

        return ApiResultHandler.buildApiResult(400, "用户名或密码错误", null);
    }
    
    /**
     * 短信验证码登录
     */
    private ApiResult smsLogin(Login login, HttpServletResponse response) {
        String phone = login.getPhone();
        String smsCode = login.getSmsCode();
        
        if (phone == null || phone.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入手机号", null);
        }
        
        if (smsCode == null || smsCode.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入短信验证码", null);
        }
        
        // 校验短信验证码
        if (!CaptchaController.verifySmsCode(phone, smsCode)) {
            return ApiResultHandler.buildApiResult(400, "短信验证码错误或已过期", null);
        }
        
        // 通过手机号查询用户
        Admin adminRes = loginMapper.findAdminByPhone(phone);
        if (adminRes != null) {
            setCookies(response, adminRes.getCardId(), "0");
            return ApiResultHandler.buildApiResult(200, "登录成功", adminRes);
        }
        
        Teacher teacherRes = loginMapper.findTeacherByPhone(phone);
        if (teacherRes != null) {
            setCookies(response, teacherRes.getCardId(), "1");
            return ApiResultHandler.buildApiResult(200, "登录成功", teacherRes);
        }
        
        Student studentRes = loginMapper.findStudentByPhone(phone);
        if (studentRes != null) {
            setCookies(response, studentRes.getCardId(), "2");
            return ApiResultHandler.buildApiResult(200, "登录成功", studentRes);
        }
        
        return ApiResultHandler.buildApiResult(400, "该手机号未绑定用户", null);
    }
    
    /**
     * 设置Cookie
     */
    private void setCookies(HttpServletResponse response, String cardId, String role) {
        Cookie token = new Cookie("rb_token", cardId);
        token.setPath("/");
        Cookie roleCookie = new Cookie("rb_role", role);
        roleCookie.setPath("/");
        response.addCookie(token);
        response.addCookie(roleCookie);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie token = new Cookie("rb_token", null);
        token.setPath("/");
        token.setMaxAge(0);
        Cookie role = new Cookie("rb_role", null);
        role.setPath("/");
        role.setMaxAge(0);
        response.addCookie(token);
        response.addCookie(role);
    }
}
