package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Score;
import com.rabbiter.oes.vo.ExamDetailVO;

import java.util.List;

public interface ScoreService {
    int add(Score score);

    List<Score> findAll();

    IPage<Score> findById(Page page, Integer studentId);

    List<Score> findById(Integer studentId);

    List<Score> findByExamCode(Integer examCode);
    Integer getPtScore(Integer examCode, Integer studentId);
    int updateEtScore(Integer examCode, Integer studentId, Integer etScore);
    
    /**
     * 获取学生某次考试的详细答卷（用于考后查看错题）
     */
    ExamDetailVO getExamDetail(Integer examCode, Integer studentId);
}
