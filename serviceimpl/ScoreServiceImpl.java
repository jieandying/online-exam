package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ExamManage;
import com.rabbiter.oes.entity.Score;
import com.rabbiter.oes.entity.SubjectiveAnswer;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.mapper.ScoreMapper;
import com.rabbiter.oes.service.ScoreService;
import com.rabbiter.oes.service.SubjectiveAnswerService;
import com.rabbiter.oes.vo.ExamDetailVO;
import com.rabbiter.oes.vo.QuestionItem;
import com.rabbiter.oes.vo.QuestionTypeDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;
    
    @Autowired
    private SubjectiveAnswerService subjectiveAnswerService;
    
    @Autowired
    private ExamManageMapper examManageMapper;
    @Override
    public int add(Score score) {
        return scoreMapper.add(score);
    }

    @Override
    public List<Score> findAll() {
        return scoreMapper.findAll();
    }

    @Override
    public IPage<Score> findById(Page page, Integer studentId) {
        return scoreMapper.findById(page, studentId);
    }

    @Override
    public List<Score> findById(Integer studentId) {
        Page<Score> scorePage = new Page<>(0, 9999);
        return scoreMapper.findById(scorePage, studentId).getRecords();
    }

    @Override
    public List<Score> findByExamCode(Integer examCode) {
        return scoreMapper.findByExamCode(examCode);
    }

    @Override
    public Integer getPtScore(Integer examCode, Integer studentId) {
        return scoreMapper.getPtScore(examCode, studentId);
    }

    @Override
    public int updateEtScore(Integer examCode, Integer studentId, Integer etScore) {
        return scoreMapper.updateEtScore(examCode, studentId, etScore);
    }

    @Override
    public ExamDetailVO getExamDetail(Integer examCode, Integer studentId) {
        // 直接按 examCode + studentId 查询，避免 findByExamCode 只返回 etScore 字段的问题
        Score myScore = scoreMapper.findByExamCodeAndStudentId(examCode, studentId);
        if (myScore == null) return null;

        // 查询考试信息获取总分
        ExamManage exam = examManageMapper.findById(examCode);
        Integer examTotalScore = (exam != null && exam.getTotalScore() != null) ? exam.getTotalScore() : 100;

        ExamDetailVO vo = new ExamDetailVO();
        vo.setExamCode(examCode);
        vo.setSubject(myScore.getSubject());
        vo.setTotalScore(examTotalScore);
        vo.setStudentScore(myScore.getEtScore() != null && myScore.getEtScore() != -1
                           ? myScore.getEtScore() : null);
        vo.setAnswerDate(myScore.getAnswerDate());

        // TODO: 后续可扩展：查询 paper 表和 answer 表组装各题型详情
        return vo;
    }
}
