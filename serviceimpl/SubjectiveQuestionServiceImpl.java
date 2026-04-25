package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.mapper.SubjectiveQuestionMapper;
import com.rabbiter.oes.service.SubjectiveQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectiveQuestionServiceImpl implements SubjectiveQuestionService {

    @Autowired
    private SubjectiveQuestionMapper subjectiveQuestionMapper;

    @Override
    public SubjectiveQuestion findById(Integer questionId) {
        return subjectiveQuestionMapper.findById(questionId);
    }

    @Override
    public IPage<SubjectiveQuestion> findAll(Page<?> page) {
        return subjectiveQuestionMapper.findAll(page);
    }

    @Override
    public List<SubjectiveQuestion> findBySubject(String subject) {
        return subjectiveQuestionMapper.findBySubject(subject);
    }

    @Override
    public List<Integer> findRandomBySubject(String subject, Integer count) {
        return subjectiveQuestionMapper.findRandomBySubject(subject, count);
    }

    @Override
    public SubjectiveQuestion findOnlyQuestionId() {
        return subjectiveQuestionMapper.findOnlyQuestionId();
    }

    @Override
    public List<SubjectiveQuestion> findByIdAndType(Integer paperId) {
        return subjectiveQuestionMapper.findByIdAndType(paperId);
    }

    @Override
    public int add(SubjectiveQuestion question) {
        return subjectiveQuestionMapper.add(question);
    }

    @Override
    public int update(SubjectiveQuestion question) {
        return subjectiveQuestionMapper.update(question);
    }

    @Override
    public int delete(Integer questionId) {
        return subjectiveQuestionMapper.delete(questionId);
    }
}
