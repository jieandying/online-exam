package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.entity.PaperManage;
import com.rabbiter.oes.entity.SubjectiveQuestion;
import com.rabbiter.oes.mapper.PaperMapper;
import com.rabbiter.oes.service.PaperService;
import com.rabbiter.oes.service.SubjectiveQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private JudgeQuestionServiceImpl judgeQuestionService;

    @Autowired
    private MultiQuestionServiceImpl multiQuestionService;

    @Autowired
    private FillQuestionServiceImpl fillQuestionService;

    @Autowired
    private SubjectiveQuestionService subjectiveQuestionService;

    @Override
    public List<PaperManage> findAll() {
        return paperMapper.findAll();
    }

    @Override
    public List<PaperManage> findById(Integer paperId) {
        return paperMapper.findById(paperId);
    }

    @Override
    public int add(PaperManage paperManage) {
        return paperMapper.add(paperManage);
    }

    @Override
    public Integer getMaxScore(Integer paperId) {
        // 获取试卷中各题型题目数量
        List<MultiQuestion> multiQuestions = multiQuestionService.findByIdAndType(paperId);      // 选择题
        List<FillQuestion> fillQuestions = fillQuestionService.findByIdAndType(paperId);         // 填空题
        List<JudgeQuestion> judgeQuestions = judgeQuestionService.findByIdAndType(paperId);       // 判断题
        List<SubjectiveQuestion> subjectiveQuestions = subjectiveQuestionService.findByIdAndType(paperId); // 主观题

        // 使用统一的分数计算器，以题目自身分值为准（如未设置则使用默认值）
        return com.rabbiter.oes.util.ScoreCalculator.calculateTotalScore(
                multiQuestions, fillQuestions, judgeQuestions, subjectiveQuestions);
    }

    @Override
    public void delete(String paperId, String type, String questionId) {
        paperMapper.delete(paperId, type, questionId);
    }

    @Override
    public void deleteByPaperId(Integer paperId) {
        paperMapper.deleteByPaperId(paperId);
    }

}
