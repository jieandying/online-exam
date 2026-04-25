package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.mapper.AnswerMapper;
import com.rabbiter.oes.service.AnswerService;
import com.rabbiter.oes.vo.AnswerVO;
import com.rabbiter.oes.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {

    @Autowired
    private AnswerMapper answerMapper;

    /**
     * 分页查询题库，按题型路由到对应表查询，支持难度过滤
     *
     * @param page     分页对象
     * @param subject  科目（"@"表示不过滤）
     * @param section  章节（"@"表示不过滤）
     * @param question 题干关键词（"@"表示不过滤）
     * @param type     题型编码（"@"表示全部，1-选择，2-填空，3-判断，4-主观）
     * @param level    难度等级（"@"表示全部，1-5）
     * @return 分页结果
     */
    @Override
    public IPage<AnswerVO> findAll(Page<AnswerVO> page, String subject, String section, String question, String type,
            String level) {
        // 将占位符"@"转为空串，使 SQL 的 LIKE '%%' 匹配全部
        subject = ("@".equals(subject) ? "" : subject);
        section = ("@".equals(section) ? "" : section);
        question = ("@".equals(question) ? "" : question);
        level = ("@".equals(level) ? "" : level);

        // 按题型路由到对应表的查询方法，实现真正的服务端分页过滤
        switch (type) {
            case "1":
                return answerMapper.findByType1(page, subject, section, question, level);
            case "2":
                return answerMapper.findByType2(page, subject, section, question, level);
            case "3":
                return answerMapper.findByType3(page, subject, section, question, level);
            case "4":
                return answerMapper.findByType4(page, subject, section, question, level);
            default:
                // 未指定题型时，UNION 查询所有表
                return answerMapper.findAll(page, subject, section, question, level);
        }
    }

    /**
     * 根据题型和ID查询单道题目详情
     *
     * @param type       题型编码
     * @param questionId 题目ID
     * @return 题目详情VO
     */
    @Override
    public QuestionVO findByIdAndType(String type, Long questionId) {
        QuestionVO questionVO = new QuestionVO();
        questionVO.setType(type);
        switch (type) {
            case "1":
            case "选择题":
                questionVO.setMultiQuestion(answerMapper.findMultiQuestionById(questionId));
                break;
            case "3":
            case "判断题":
                questionVO.setJudgeQuestion(answerMapper.findJudgeQuestionById(questionId));
                break;
            case "2":
            case "填空题":
                questionVO.setFillQuestion(answerMapper.findFillQuestionById(questionId));
                break;
            case "4":
            case "主观题":
                questionVO.setSubjectiveQuestion(answerMapper.findSubjectiveQuestionById(questionId));
                break;
        }
        return questionVO;
    }
}
