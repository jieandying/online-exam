package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
public class RegisterController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @PostMapping("/register")
    public ApiResult register(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String smsCode = params.get("smsCode");
        String role = params.get("role");
        String username = params.get("username");
        String password = params.get("password");

        // 验证参数
        if (phone == null || phone.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入手机号", null);
        }

        if (smsCode == null || smsCode.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入验证码", null);
        }

        if (role == null || role.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请选择角色", null);
        }

        if (username == null || username.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入用户名", null);
        }

        if (password == null || password.isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "请输入密码", null);
        }

        // 验证短信验证码
        if (!CaptchaController.verifySmsCode(phone, smsCode)) {
            return ApiResultHandler.buildApiResult(400, "验证码错误或已过期", null);
        }

        // 检查手机号是否已注册
        if (isPhoneExists(phone)) {
            return ApiResultHandler.buildApiResult(400, "该手机号已被注册", null);
        }

        // 生成随机ID（6位数）
        int randomId = 100000 + new Random().nextInt(900000);

        // 根据角色创建账号
        if ("student".equals(role)) {
            Student student = new Student();
            student.setStudentId(randomId);
            student.setStudentName(username);
            student.setTel(phone);
            student.setPwd(password);
            student.setCardId(String.valueOf(randomId));
            student.setRole("2");
            student.setGrade("2024");
            student.setMajor("未设置");
            student.setClazz("未设置");
            student.setInstitute("未设置");
            student.setSex("保密");

            try {
                studentMapper.add(student);
                return ApiResultHandler.buildApiResult(200, "注册成功", student);
            } catch (Exception e) {
                e.printStackTrace();
                return ApiResultHandler.buildApiResult(500, "注册失败：" + e.getMessage(), null);
            }

        } else if ("teacher".equals(role)) {
            Teacher teacher = new Teacher();
            teacher.setTeacherId(randomId);
            teacher.setTeacherName(username);
            teacher.setTel(phone);
            teacher.setPwd(password);
            teacher.setCardId(String.valueOf(randomId));
            teacher.setRole("1");
            teacher.setInstitute("未设置");
            teacher.setSex("保密");
            teacher.setType("讲师");

            try {
                teacherMapper.add(teacher);
                return ApiResultHandler.buildApiResult(200, "注册成功", teacher);
            } catch (Exception e) {
                e.printStackTrace();
                return ApiResultHandler.buildApiResult(500, "注册失败：" + e.getMessage(), null);
            }

        } else {
            return ApiResultHandler.buildApiResult(400, "无效的角色类型", null);
        }
    }

    /**
     * 检查手机号是否已存在
     */
    private boolean isPhoneExists(String phone) {
        // 检查学生表
        try {
            Student student = studentMapper.findByPhone(phone);
            if (student != null) {
                return true;
            }
        } catch (Exception e) {
            // 忽略，继续检查教师表
        }

        // 检查教师表
        try {
            Teacher teacher = teacherMapper.findByPhone(phone);
            if (teacher != null) {
                return true;
            }
        } catch (Exception e) {
            // 忽略
        }

        return false;
    }
}
