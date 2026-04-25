package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Student;

public interface StudentService {

    IPage<Student> findAll(Page<Student> page, String name, String grade,
                           String tel, String institute, String major, String clazz);

    Student findById(Integer studentId);

    int deleteById(Integer studentId);

    int update(Student student);

    int updatePwd(Student student);
    int add(Student student);
    
    /**
     * 更新学生人脸数据
     */
    int updateFaceData(Integer studentId, String faceData);
    
    /**
     * 获取学生人脸数据
     */
    String getFaceData(Integer studentId);

    /** 获取所有班级名称（不重复） */
    java.util.List<String> findAllClazz();
}
