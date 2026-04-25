package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SubjectiveAnswer;
import com.rabbiter.oes.entity.SubjectiveQuestion;

import java.util.List;

public interface SubjectiveAnswerService {
    SubjectiveAnswer findById(Integer answerId);
    IPage<SubjectiveAnswer> findPendingByExam(Page<?> page, Integer examCode);
    IPage<SubjectiveAnswer> findAllPending(Page<?> page);
    List<SubjectiveAnswer> findByExamAndStudent(Integer examCode, Integer studentId);
    int countPendingByExam(Integer examCode);
    int countPendingByExamAndStudent(Integer examCode, Integer studentId);
    int add(SubjectiveAnswer answer);
    int score(SubjectiveAnswer answer);
    int delete(Integer answerId);
    int getTotalScore(Integer examCode, Integer studentId);
    
    IPage<SubjectiveAnswer> findAllGraded(Page<?> page);
    IPage<SubjectiveAnswer> findGradedByExam(Page<?> page, Integer examCode);

    /**
     * 根据 ID 获取主观题题目信息
     * @param questionId 题目 ID
     * @return 题目信息
     */
    SubjectiveQuestion getQuestionById(Integer questionId);
}
