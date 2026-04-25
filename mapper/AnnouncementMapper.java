package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Select("SELECT * FROM announcement WHERE status = 1 AND (target_role = 'all' OR target_role = #{role}) ORDER BY create_time DESC")
    IPage<Announcement> findByRole(Page page, @Param("role") String role);

    @Select("SELECT * FROM announcement ORDER BY create_time DESC")
    IPage<Announcement> findAll(Page page);

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement findById(@Param("id") Integer id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO announcement(title, content, publisher_id, publisher_name, publisher_role, target_role, status) " +
            "VALUES(#{title}, #{content}, #{publisherId}, #{publisherName}, #{publisherRole}, #{targetRole}, #{status})")
    int add(Announcement announcement);

    @Update("UPDATE announcement SET title=#{title}, content=#{content}, target_role=#{targetRole}, status=#{status} WHERE id=#{id}")
    int update(Announcement announcement);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int delete(@Param("id") Integer id);

    @Select("SELECT COUNT(*) FROM announcement WHERE status = 1")
    int countPublished();
}
