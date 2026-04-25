package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SystemLog;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SystemLogMapper {

    @Select("SELECT * FROM system_log ORDER BY create_time DESC")
    IPage<SystemLog> findAll(Page page);

    @Select("<script>SELECT * FROM system_log WHERE 1=1 " +
            "<if test='action != null and action != \"\"'> AND action LIKE CONCAT('%',#{action},'%')</if> " +
            "<if test='operatorName != null and operatorName != \"\"'> AND operator_name LIKE CONCAT('%',#{operatorName},'%')</if> " +
            "ORDER BY create_time DESC</script>")
    IPage<SystemLog> search(Page page, @Param("action") String action, @Param("operatorName") String operatorName);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO system_log(operator_id, operator_name, operator_role, action, detail, ip) " +
            "VALUES(#{operatorId}, #{operatorName}, #{operatorRole}, #{action}, #{detail}, #{ip})")
    int add(SystemLog systemLog);

    @Delete("DELETE FROM system_log WHERE id = #{id}")
    int delete(@Param("id") Integer id);

    @Select("SELECT COUNT(*) FROM system_log")
    int count();
}
