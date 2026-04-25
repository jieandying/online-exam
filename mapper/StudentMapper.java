package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Student;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StudentMapper {

    /**
     * 查询所有学生（不分页）
     * @return List<Student>
     */
    @Select("select * from student")
    java.util.List<Student> findAll();

    /**
     * 分页查询所有学生
     * @param page
     * @return List<Student>
     */
    @Select("select * from student where " +
            "studentName like concat('%',#{name},'%') " +
            "and grade like concat('%',#{grade},'%') " +
            "and tel like concat('%',#{tel},'%') " +
            "and major like concat('%',#{major},'%') " +
            "and institute like concat('%',#{institute},'%') " +
            "and clazz like concat('%',#{clazz},'%')")
    IPage<Student> findAllPaged(Page page, @Param("name") String name, @Param("grade") String grade,
                           @Param("tel") String tel,  @Param("institute") String institute,
                           @Param("major")String major, @Param("clazz") String clazz);

    @Select("select * from student where studentId = #{studentId}")
    Student findById(Integer studentId);

    @Delete("delete from student where studentId = #{studentId}")
    int deleteById(Integer studentId);

    /**
     *更新所有学生信息
     * @param student 传递一个对象
     * @return 受影响的记录条数
     */
    @Update("update student set studentName = #{studentName},grade = #{grade},major = #{major},clazz = #{clazz}," +
            "institute = #{institute},tel = #{tel},email = #{email},pwd = #{pwd},cardId = #{cardId},sex = #{sex},role = #{role} " +
            "where studentId = #{studentId}")
    int update(Student student);

    /**
     * 更新密码
     * @param student
     * @return 受影响的记录条数
     */
    @Update("update student set pwd = #{pwd} where studentId = #{studentId}")
    int updatePwd(Student student);


    @Options(useGeneratedKeys = true,keyProperty = "studentId")
    @Insert("insert into student(studentName,grade,major,clazz,institute,tel,email,pwd,cardId,sex,role) values " +
            "(#{studentName},#{grade},#{major},#{clazz},#{institute},#{tel},#{email},#{pwd},#{cardId},#{sex},#{role})")
    int add(Student student);

    /**
     * 更新学生人脸数据
     * @param studentId 学生ID
     * @param faceData 人脸数据（Base64编码）
     * @return 受影响的记录条数
     */
    @Update("update student set faceData = #{faceData} where studentId = #{studentId}")
    int updateFaceData(@Param("studentId") Integer studentId, @Param("faceData") String faceData);

    /**
     * 获取学生人脸数据
     * @param studentId 学生ID
     * @return 人脸数据
     */
    @Select("select faceData from student where studentId = #{studentId}")
    String getFaceData(@Param("studentId") Integer studentId);

    /**
     * 通过ID直接更新密码（忘记密码功能）
     * @param studentId 学生ID
     * @param password 新密码
     * @return 受影响的记录条数
     */
    @Update("update student set pwd = #{password} where studentId = #{studentId}")
    int updatePassword(@Param("studentId") Integer studentId, @Param("password") String password);

    /**
     * 通过手机号查找学生
     * @param phone 手机号
     * @return 学生对象
     */
    @Select("select * from student where tel = #{phone}")
    Student findByPhone(@Param("phone") String phone);

    /** 查询所有不重复的班级名称 */
    @Select("select distinct clazz from student where clazz is not null and clazz != '' order by clazz")
    java.util.List<String> findAllClazz();
}
