package org.example.backend.service;

import org.example.backend.dto.DoctorAccountDTO;
import org.example.backend.dto.DoctorQueryDTO;
import org.example.backend.dto.PageResult;
import org.example.backend.pojo.DoctorBioUpdateRequest;

import java.util.List;

public interface DoctorAccountService {

    /**
     * 查询医生列表(支持筛选)
     */
    PageResult<DoctorAccountDTO> getDoctorList(DoctorQueryDTO queryDTO);

    /**
     * 新增医生账号
     */
    void addDoctor(DoctorAccountDTO doctorDTO);

    /**
     * 修改医生信息
     */
    void updateDoctor(DoctorAccountDTO doctorDTO);

    /**
     * 修改医生账号信息--users表中的status字段，以判断启用/停用
     * 
     * @param doctorId
     * @param status
     */
    void updateDoctorAccountStatus(Long doctorId, String status);

    /**
     * 修改医生任职信息
     */
    void updateDoctorStatus(Long doctorId, String status);

    /**
     * 重置医生密码
     */
    void resetPassword(Long doctorId);

    /**
     * 根据ID查询医生详情
     */
    DoctorAccountDTO getDoctorById(Long doctorId);

    // -----医生端-----//

    /**
     * 医生提交修改bio申请
     */
    void submitBioChange(Long userId, String newBio);

    // -----管理端-----//
    /**
     * 管理员审核bio修改申请
     */
    void reviewRequest(Long requestId, boolean approved, String reason);

    /**
     * 获取所有待审批的bio修改
     */
    List<DoctorBioUpdateRequest> getPendingBioRequests();

    /**
     * 获取所有bio修改申请（用于管理端筛选）
     */
    List<DoctorBioUpdateRequest> getAllBioRequests();

    /**
     * 获取详细bio修改申请
     */
    DoctorBioUpdateRequest getBioRequestDetail(Long requestId);

}