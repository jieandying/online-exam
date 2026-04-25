package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SubjectiveQuestion;

import java.util.List;

public interface SubjectiveQuestionService {
    SubjectiveQuestion findById(Integer questionId);

    IPage<SubjectiveQuestion> findAll(Page<?> page);

    List<SubjectiveQuestion> findBySubject(String subject);

    List<Integer> findRandomBySubject(String subject, Integer count);

    SubjectiveQuestion findOnlyQuestionId();

    List<SubjectiveQuestion> findByIdAndType(Integer paperId);

    int add(SubjectiveQuestion question);

    int update(SubjectiveQuestion question);

    int delete(Integer questionId);
}
