package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.backend.dto.DoctorAccountDTO;
import org.example.backend.dto.DoctorQueryDTO;
import org.example.backend.dto.DoctorVO;
import org.example.backend.pojo.Doctor;

import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    /**
     * 模糊搜索医生：匹配医生姓名、科室名、描述
     */
    @Mapper
    @Select("SELECT d.doctor_id AS doctorId, u.username AS doctorName, dept.dept_name AS deptName, d.title, d.bio " +
            "FROM doctors d " +
            "LEFT JOIN users u ON d.user_id = u.user_id " +
            "LEFT JOIN departments dept ON d.dept_id = dept.dept_id " +
            "WHERE u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR dept.dept_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR d.bio LIKE CONCAT('%', #{keyword}, '%')")
    List<DoctorVO> searchDoctors(@Param("keyword") String keyword);

    @Select(""" 
    SELECT 
        d.doctor_id AS doctorId, d.user_id AS userId, d.dept_id AS deptId, 
        dep.dept_name AS deptName, d.title, d.bio,
        d.status AS doctorStatus, d.created_at AS createdAt, d.updated_at AS updatedAt,
        u.username, u.phone, u.email, u.gender,
        u.status AS userStatus
    FROM doctors d
    INNER JOIN users u ON d.user_id = u.user_id
    LEFT JOIN departments dep ON d.dept_id = dep.dept_id
    WHERE (#{deptId} IS NULL OR d.dept_id = #{deptId})
      AND (#{username} IS NULL OR u.username LIKE CONCAT('%', #{username}, '%'))
      AND (#{status} IS NULL OR u.status = #{status})
      AND (#{doctorStatus} IS NULL OR d.status = #{doctorStatus})
    ORDER BY d.created_at DESC
""")
    List<DoctorAccountDTO> selectDoctorList(DoctorQueryDTO queryDTO);



    /**
     * 根据 doctorId 查询医生详情
     */
    @Select("""
    SELECT 
        d.doctor_id AS doctorId,
        d.user_id AS userId,
        d.dept_id AS deptId,
        dep.dept_name AS deptName,
        d.title,
        d.bio,
        d.status AS doctorStatus,
        d.created_at AS createdAt,
        d.updated_at AS updatedAt,
        u.username,
        u.phone,
        u.email,
        u.gender,
        u.status AS userStatus
    FROM doctors d
    INNER JOIN users u ON d.user_id = u.user_id
    LEFT JOIN departments dep ON d.dept_id = dep.dept_id
    WHERE d.doctor_id = #{doctorId}
""")
    DoctorAccountDTO selectDoctorWithUserById(@Param("doctorId") Long doctorId);



    /**
     * 插入医生
     */
    @Insert("""
        INSERT INTO doctors (user_id, dept_id, title, bio, status)
        VALUES (#{userId}, #{deptId}, #{title}, #{bio}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "doctorId")
    int insert(Doctor doctor);


    /**
     * 更新医生
     */
    @Update("""
        UPDATE doctors
        SET dept_id = COALESCE(#{deptId}, dept_id),
            title = COALESCE(#{title}, title),
            bio = COALESCE(#{bio}, bio),
            status = COALESCE(#{status}, status),
            updated_at = #{updatedAt}
        WHERE doctor_id = #{doctorId}
    """)
    int updateById(Doctor doctor);

}
