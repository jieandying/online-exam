package com.rabbiter.oes.vo;

import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.entity.SubjectiveQuestion;

public class QuestionVO {
    private String type;

    private FillQuestion fillQuestion;

    private JudgeQuestion judgeQuestion;

    private MultiQuestion multiQuestion;

    private SubjectiveQuestion subjectiveQuestion;

    public SubjectiveQuestion getSubjectiveQuestion() {
        return subjectiveQuestion;
    }

    public void setSubjectiveQuestion(SubjectiveQuestion subjectiveQuestion) {
        this.subjectiveQuestion = subjectiveQuestion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FillQuestion getFillQuestion() {
        return fillQuestion;
    }

    public void setFillQuestion(FillQuestion fillQuestion) {
        this.fillQuestion = fillQuestion;
    }

    public JudgeQuestion getJudgeQuestion() {
        return judgeQuestion;
    }

    public void setJudgeQuestion(JudgeQuestion judgeQuestion) {
        this.judgeQuestion = judgeQuestion;
    }

    public MultiQuestion getMultiQuestion() {
        return multiQuestion;
    }

    public void setMultiQuestion(MultiQuestion multiQuestion) {
        this.multiQuestion = multiQuestion;
    }
}
