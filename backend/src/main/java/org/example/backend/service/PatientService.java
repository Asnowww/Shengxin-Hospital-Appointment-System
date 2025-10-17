package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.PatientUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.Patient;

/**
 * 患者业务逻辑接口
 */
public interface PatientService extends IService<Patient> {

    /**
     * 根据用户ID查询患者信息
     * 
     * @param userId 用户ID
     * @return 患者信息
     */
    Patient getPatientByUserId(Long userId);

    /**
     * 更新患者个人信息
     * 
     * @param userId 用户ID
     * @param param  更新参数
     * @return 统一返回结构
     */
    Result<Void> updatePatientInfo(Long userId, PatientUpdateParam param);
}
