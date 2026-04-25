package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.AiQuestionRecord;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AiQuestionRecordMapper {

    @Select("SELECT * FROM ai_question_record ORDER BY create_time DESC")
    IPage<AiQuestionRecord> findAll(Page<AiQuestionRecord> page);

    @Select("SELECT * FROM ai_question_record WHERE id = #{id}")
    AiQuestionRecord findById(Integer id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO ai_question_record(subject, question_type, question_count, difficulty, saved_count, questions_json) " +
            "VALUES(#{subject}, #{questionType}, #{questionCount}, #{difficulty}, #{savedCount}, #{questionsJson})")
    int add(AiQuestionRecord record);

    @Delete("DELETE FROM ai_question_record WHERE id = #{id}")
    int delete(Integer id);

    @Update("UPDATE ai_question_record SET saved_count = #{savedCount} WHERE id = #{id}")
    int updateSavedCount(@Param("id") Integer id, @Param("savedCount") Integer savedCount);
}
