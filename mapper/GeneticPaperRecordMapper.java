package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.GeneticPaperRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GeneticPaperRecordMapper {

    @Select("SELECT * FROM genetic_paper_record ORDER BY create_time DESC")
    IPage<GeneticPaperRecord> findAll(Page<GeneticPaperRecord> page);

    @Select("SELECT * FROM genetic_paper_record WHERE id = #{id}")
    GeneticPaperRecord findById(Integer id);

    /**
     * 按科目查询历史组卷记录（用于相似度计算）
     * subjects 字段是逗号分隔的科目名，使用 LIKE 做模糊匹配
     */
    @Select("SELECT id, subjects, paper_json FROM genetic_paper_record " +
            "WHERE subjects LIKE CONCAT('%', #{subject}, '%') ORDER BY create_time DESC LIMIT 50")
    List<GeneticPaperRecord> findBySubject(String subject);

    /**
     * 查询最近 N 条记录用于多科目相似度检测
     */
    @Select("SELECT id, subjects, paper_json FROM genetic_paper_record ORDER BY create_time DESC LIMIT #{limit}")
    List<GeneticPaperRecord> findLatest(Integer limit);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO genetic_paper_record(subjects, target_difficulty, multi_count, fill_count, judge_count, " +
            "total_score, total_questions, avg_difficulty, fitness, generation_time, paper_json, student_id, create_time) "
            +
            "VALUES(#{subjects}, #{targetDifficulty}, #{multiCount}, #{fillCount}, #{judgeCount}, " +
            "#{totalScore}, #{totalQuestions}, #{avgDifficulty}, #{fitness}, #{generationTime}, #{paperJson}, #{studentId}, #{createTime})")
    int add(GeneticPaperRecord record);

    @Delete("DELETE FROM genetic_paper_record WHERE id = #{id}")
    int delete(Integer id);

    @Update("UPDATE genetic_paper_record SET is_created_exam = 1, exam_code = #{examCode} WHERE id = #{id}")
    int updateExamCreated(@Param("id") Integer id, @Param("examCode") Integer examCode);
}
