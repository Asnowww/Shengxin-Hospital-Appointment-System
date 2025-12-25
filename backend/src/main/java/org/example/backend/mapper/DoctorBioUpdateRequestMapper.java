package org.example.backend.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.pojo.DoctorBioUpdateRequest;

import java.util.List;

@Mapper
@TableName("doctor_bio_update_request")
public interface DoctorBioUpdateRequestMapper extends BaseMapper<DoctorBioUpdateRequest> {
    /**
     * 查询所有待审批的 bio 修改申请
     */
    @Select("""
            SELECT r.*, u.username AS doctorName
            FROM doctor_bio_update_request r
            JOIN doctors d ON r.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE r.status = 'pending'
            ORDER BY r.created_at DESC
            """)
    List<DoctorBioUpdateRequest> selectPendingRequests();

    /**
     * 根据 requestId 查询申请详情
     */
    @Select("""
            SELECT r.*, u.username AS doctorName
            FROM doctor_bio_update_request r
            JOIN doctors d ON r.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            WHERE r.id = #{requestId}
            """)
    DoctorBioUpdateRequest selectById(Long requestId);

    /**
     * 查询所有 bio 修改申请（用于管理端筛选）
     */
    @Select("""
            SELECT r.*, u.username AS doctorName
            FROM doctor_bio_update_request r
            JOIN doctors d ON r.doctor_id = d.doctor_id
            JOIN users u ON d.user_id = u.user_id
            ORDER BY r.created_at DESC
            """)
    List<DoctorBioUpdateRequest> selectAllRequests();

}
