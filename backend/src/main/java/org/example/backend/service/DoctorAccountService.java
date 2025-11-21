package org.example.backend.service;

import org.example.backend.dto.DoctorAccountDTO;
import org.example.backend.dto.DoctorQueryDTO;

import java.util.List;
import java.util.stream.Collectors;

public interface DoctorAccountService {

    /**
     * 查询医生列表(支持筛选)
     */
    List<DoctorAccountDTO> getDoctorList(DoctorQueryDTO queryDTO);

    /**
     * 新增医生账号
     */
    void addDoctor(DoctorAccountDTO doctorDTO);

    /**
     * 修改医生信息
     */
    void updateDoctor(DoctorAccountDTO doctorDTO);

    /**
     * 启用 / 禁用医生账号
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

    //-----医生端-----//

    /**
     * 医生提交修改bio申请
     */
    void submitBioChange(Long doctorId, String newBio);

    //-----管理端-----//
    /**
     * 管理员审核bio修改申请
     */
    void reviewRequest(Long requestId, boolean approved, String reason);
}