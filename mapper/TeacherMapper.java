package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Teacher;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeacherMapper {

    @Select("select * from teacher")
    IPage<Teacher>  findAll(Page page);

//    @Select("select * from teacher")
//    public List<Teacher> findAll();

    @Select("select * from teacher where teacherId = #{teacherId}")
    public Teacher findById(Integer teacherId);

    @Delete("delete from teacher where teacherId = #{teacherId}")
    public int deleteById(Integer teacherId);

    @Update("update teacher set teacherName = #{teacherName},sex = #{sex}," +
            "tel = #{tel}, email = #{email},pwd = #{pwd},cardId = #{cardId}," +
            "role = #{role},institute = #{institute},type = #{type} where teacherId = #{teacherId}")
    public int update(Teacher teacher);

    @Options(useGeneratedKeys = true,keyProperty = "teacherId")
    @Insert("insert into teacher(teacherName,sex,tel,email,pwd,cardId,role,type,institute) " +
            "values(#{teacherName},#{sex},#{tel},#{email},#{pwd},#{cardId},#{role},#{type},#{institute})")
    public int add(Teacher teacher);

    /**
     * 通过ID直接更新密码（忘记密码功能）
     * @param teacherId 教师ID
     * @param password 新密码
     * @return 受影响的记录条数
     */
    @Update("update teacher set pwd = #{password} where teacherId = #{teacherId}")
    int updatePassword(@Param("teacherId") Integer teacherId, @Param("password") String password);

    /**
     * 通过手机号查找教师
     * @param phone 手机号
     * @return 教师对象
     */
    @Select("select * from teacher where tel = #{phone}")
    Teacher findByPhone(@Param("phone") String phone);
}
