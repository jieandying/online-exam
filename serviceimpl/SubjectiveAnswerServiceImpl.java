package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SubjectiveAnswer;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.mapper.SubjectiveAnswerMapper;
import com.rabbiter.oes.mapper.SubjectiveQuestionMapper;
import com.rabbiter.oes.service.SubjectiveAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectiveAnswerServiceImpl implements SubjectiveAnswerService {

    @Autowired
    private SubjectiveAnswerMapper subjectiveAnswerMapper;
    
    @Autowired
    private SubjectiveQuestionMapper subjectiveQuestionMapper;

    @Override
    public SubjectiveAnswer findById(Integer answerId) {
        return subjectiveAnswerMapper.findById(answerId);
    }

    @Override
    public IPage<SubjectiveAnswer> findPendingByExam(Page<?> page, Integer examCode) {
        return subjectiveAnswerMapper.findPendingByExam(page, examCode);
    }

    @Override
    public IPage<SubjectiveAnswer> findAllPending(Page<?> page) {
        return subjectiveAnswerMapper.findAllPending(page);
    }

    @Override
    public List<SubjectiveAnswer> findByExamAndStudent(Integer examCode, Integer studentId) {
        return subjectiveAnswerMapper.findByExamAndStudent(examCode, studentId);
    }

    @Override
    public int countPendingByExam(Integer examCode) {
        return subjectiveAnswerMapper.countPendingByExam(examCode);
    }

    @Override
    public int countPendingByExamAndStudent(Integer examCode, Integer studentId) {
        return subjectiveAnswerMapper.countPendingByExamAndStudent(examCode, studentId);
    }

    @Override
    public int add(SubjectiveAnswer answer) {
        return subjectiveAnswerMapper.add(answer);
    }

    @Override
    public int score(SubjectiveAnswer answer) {
        return subjectiveAnswerMapper.score(answer);
    }

    @Override
    public int delete(Integer answerId) {
        return subjectiveAnswerMapper.delete(answerId);
    }

    @Override
    public int getTotalScore(Integer examCode, Integer studentId) {
        return subjectiveAnswerMapper.getTotalScore(examCode, studentId);
    }

    @Override
    public IPage<SubjectiveAnswer> findAllGraded(Page<?> page) {
        return subjectiveAnswerMapper.findAllGraded(page);
    }

    @Override
    public IPage<SubjectiveAnswer> findGradedByExam(Page<?> page, Integer examCode) {
        return subjectiveAnswerMapper.findGradedByExam(page, examCode);
    }
    
    /**
     * 根据 ID 获取主观题题目信息
     * @param questionId 题目 ID
     * @return 题目信息
     */
    @Override
    public SubjectiveQuestion getQuestionById(Integer questionId) {
        return subjectiveQuestionMapper.findById(questionId);
    }
}
