package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ViolationRecord;

import java.util.List;
import java.util.Map;

/**
 * 违规检测服务接口
 */
public interface ViolationService {

    /**
     * 记录违规行为
     */
    int recordViolation(ViolationRecord record);

    /**
     * 查询学生在某次考试中的违规记录
     */
    List<ViolationRecord> getViolationsByStudentAndExam(Integer studentId, Integer examCode);

    /**
     * 查询某次考试的所有违规记录
     */
    List<ViolationRecord> getViolationsByExam(Integer examCode);

    /**
     * 分页查询某次考试的违规记录
     */
    IPage<ViolationRecord> getViolationsByExamPage(Page<ViolationRecord> page, Integer examCode);

    /**
     * 获取学生在某次考试中的违规统计
     */
    Map<String, Object> getViolationStats(Integer studentId, Integer examCode);

    /**
     * 检查学生是否超过违规阈值
     */
    boolean isViolationExceeded(Integer studentId, Integer examCode, int threshold);

    /**
     * 删除违规记录
     */
    int deleteViolation(Integer id);

    /**
     * 删除考试的所有违规记录
     */
    int deleteViolationsByExam(Integer examCode);
}
