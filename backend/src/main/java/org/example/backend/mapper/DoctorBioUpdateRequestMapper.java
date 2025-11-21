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
    @Select("SELECT * FROM doctor_bio_update_request WHERE status = 'pending' ORDER BY created_at ")
    List<DoctorBioUpdateRequest> selectPendingRequests();

    /**
     * 根据 requestId 查询申请详情
     */
    @Select("SELECT * FROM doctor_bio_update_request WHERE id = #{requestId}")
    DoctorBioUpdateRequest selectById(Long requestId);

}
