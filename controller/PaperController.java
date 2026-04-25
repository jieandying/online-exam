package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.serviceimpl.FillQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.JudgeQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.MultiQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.PaperServiceImpl;
import com.rabbiter.oes.serviceimpl.SubjectiveQuestionServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PaperController {

    @Autowired
    private PaperServiceImpl paperService;

    @Autowired
    private JudgeQuestionServiceImpl judgeQuestionService;

    @Autowired
    private MultiQuestionServiceImpl multiQuestionService;

    @Autowired
    private FillQuestionServiceImpl fillQuestionService;

    @Autowired
    private SubjectiveQuestionServiceImpl subjectiveQuestionService;

    @Autowired
    private ExamManageMapper examManageMapper;

    @GetMapping("/papers")
    public ApiResult<PaperManage> findAll() {
       ApiResult res =  ApiResultHandler.buildApiResult(200,"请求成功",paperService.findAll());
       return  res;
    }

    @GetMapping("/paper/{paperId}")
    public Map<Integer, List<?>> findById(@PathVariable("paperId") Integer paperId) {
        List<MultiQuestion> multiQuestionRes = multiQuestionService.findByIdAndType(paperId);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findByIdAndType(paperId);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findByIdAndType(paperId);   //判断题题库 3
        List<SubjectiveQuestion> subjectiveQuestionRes = subjectiveQuestionService.findByIdAndType(paperId);   //主观题题库 4
        Map<Integer, List<?>> map = new HashMap<>();
        map.put(1,multiQuestionRes);
        map.put(2,fillQuestionsRes);
        map.put(3,judgeQuestionRes);
        map.put(4,subjectiveQuestionRes);
        return  map;
    }

    /**
     * 按考试编号获取试卷题目，并用考试配置的分值覆盖题目表原始分值。
     * 该接口确保学生答题时、试题详情页的分值与组卷时的配置完全一致。
     */
    @GetMapping("/exam-paper/{examCode}")
    public Map<Integer, List<?>> findByExamCode(@PathVariable("examCode") Integer examCode) {
        ExamManage exam = examManageMapper.findById(examCode);
        if (exam == null || exam.getPaperId() == null) return new HashMap<>();

        int paperId = exam.getPaperId();
        // 优先使用 exam_manage 中存储的分值配置，如果为 null 则使用默认值
        final int mScore = exam.getMultiScore() != null ? exam.getMultiScore() : 2;
        final int fScore = exam.getFillScore()  != null ? exam.getFillScore()  : 5;
        final int jScore = exam.getJudgeScore() != null ? exam.getJudgeScore() : 5;
        final int sScore = exam.getSubjectiveScore() != null ? exam.getSubjectiveScore() : 10;

        List<MultiQuestion> multiQuestionRes = multiQuestionService.findByIdAndType(paperId);
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findByIdAndType(paperId);
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findByIdAndType(paperId);
        List<SubjectiveQuestion> subjectiveQuestionRes = subjectiveQuestionService.findByIdAndType(paperId);

        // 用考试配置分值覆盖题目表原始分值，确保指向同一题目的不同考试分值各自独立
        multiQuestionRes.forEach(q -> q.setScore(mScore));
        fillQuestionsRes.forEach(q -> q.setScore(fScore));
        judgeQuestionRes.forEach(q -> q.setScore(jScore));
        subjectiveQuestionRes.forEach(q -> q.setScore(sScore));

        Map<Integer, List<?>> map = new HashMap<>();
        map.put(1, multiQuestionRes);
        map.put(2, fillQuestionsRes);
        map.put(3, judgeQuestionRes);
        map.put(4, subjectiveQuestionRes);
        return map;
    }

    @PostMapping("/paperManage")
    public ApiResult add(@RequestBody PaperManage paperManage) {
        int res = paperService.add(paperManage);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"添加失败",res);
    }

    /**
     * 删除试卷中的某条试题
     *
     * @param paperId 试卷id
     * @param type 题目类型。1选择，2填空，3判断
     * @param questionId 题目id
     */
    @GetMapping("/paper/delete/{paperId}/{type}/{questionId}")
    public ApiResult delete(
            @PathVariable("paperId") String paperId,
            @PathVariable("type") String type,
            @PathVariable("questionId") String questionId
    ) {
        paperService.delete(paperId, type, questionId);
        return ApiResultHandler.buildApiResult(200,"删除成功", null);
    }
}
