package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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


}
