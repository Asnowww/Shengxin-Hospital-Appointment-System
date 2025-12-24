package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.AppointmentInfoDTO;
import org.example.backend.dto.MedicalRecordDTO;
import org.example.backend.dto.MedicalRecordParam;
import org.example.backend.dto.VisitRecordDTO;
import org.example.backend.mapper.AppointmentMapper;
import org.example.backend.mapper.MedicalRecordMapper;
import org.example.backend.pojo.MedicalRecord;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.MedicalRecordService;
import org.example.backend.service.PatientService;
import org.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    @Resource
    private MedicalRecordMapper medicalRecordMapper;

    @Resource
    private PatientService patientService;
    @Autowired
    private UserService userService;

    @Resource
    private AppointmentMapper appointmentMapper;// 👈 复用旧 service
    /**
     * 根据患者id获取病历信息
     */
    @Override
    public MedicalRecordDTO getLatestByPatientId(Long patientId) {
        if (patientId == null) return null;

        // 1. 查询患者信息
        Patient patient = patientService.getById(patientId);
        if (patient == null) return null;

        // 2. 查询患者最新一条病历
        MedicalRecord record = medicalRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .orderByDesc(MedicalRecord::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (record == null) return null;

        // 3. 转 DTO 并填充患者信息
        MedicalRecordDTO dto = new MedicalRecordDTO();
        BeanUtils.copyProperties(record, dto);
        dto.setHeight(patient.getHeight());
        dto.setWeight(patient.getWeight());
        dto.setAddress(patient.getAddress());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setBirthDate(patient.getBirthDate());

        return dto;
    }


    /**
     * 根据患者ID获取历史病历列表（按创建时间倒序，限制条数）
     */
    @Override
    public List<MedicalRecordDTO> getHistoryByPatientId(Long patientId, Integer limit) {
        List<MedicalRecord> list = medicalRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .orderByDesc(MedicalRecord::getCreatedAt)
                        .last("LIMIT " + limit)
        );

        return list.stream().map(r -> {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            BeanUtils.copyProperties(r, dto);

            // 填充患者信息
            Patient patient = patientService.getById(r.getPatientId());
            if (patient != null) {
                dto.setBirthDate(patient.getBirthDate());
                dto.setHeight(patient.getHeight());
                dto.setWeight(patient.getWeight());
                dto.setAddress(patient.getAddress());
                dto.setMedicalHistory(patient.getMedicalHistory());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 创建病历（参考患者信息）
     */
    @Override
    public MedicalRecordDTO createRecord(MedicalRecordParam param) {
        // 1. 查询患者信息
        Patient patient = patientService.getById(param.getPatientId());
        User user=userService.getById(patient.getUserId());
        if (patient == null) throw new RuntimeException("患者不存在");

        // 2. 创建病历
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patient.getPatientId());

        // 复制医生填写的病历信息
        BeanUtils.copyProperties(param, record, "patientId"); // 忽略 patientId 避免覆盖

        // 3. 设置默认值，防止数据库报错
        if (record.getChiefComplaint() == null) record.setChiefComplaint("未填写");
        if (record.getDiagnosis() == null) record.setDiagnosis("未填写");

        // 4. 插入数据库
        medicalRecordMapper.insert(record);

        // 5. 转 DTO 返回
        MedicalRecordDTO dto = new MedicalRecordDTO();
        BeanUtils.copyProperties(record, dto);

        // 填充患者信息
        dto.setBirthDate(patient.getBirthDate());
        dto.setHeight(patient.getHeight());
        dto.setWeight(patient.getWeight());
        dto.setAddress(patient.getAddress());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setGender(user.getGender());

        return dto;
    }

    /**
     * 更新病历（保留患者信息不覆盖）
     */
    @Override
    public MedicalRecordDTO updateRecord(Long recordId, MedicalRecordParam param) {
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null) return null;

        // 更新医生填写的病历信息
        BeanUtils.copyProperties(param, record, "patientId"); // 保留 patientId
        medicalRecordMapper.updateById(record);

        // 查询患者信息
        Patient patient = patientService.getById(record.getPatientId());

        // 填充 DTO
        MedicalRecordDTO dto = new MedicalRecordDTO();
        BeanUtils.copyProperties(record, dto);
        if (patient != null) {
            dto.setBirthDate(patient.getBirthDate());
            dto.setHeight(patient.getHeight());
            dto.setWeight(patient.getWeight());
            dto.setAddress(patient.getAddress());
            dto.setMedicalHistory(patient.getMedicalHistory());
        }

        return dto;
    }

    @Override
    public List<VisitRecordDTO> getVisitRecordsByPatient(Long patientId, Integer limit) {

        // 1. 查询已完成的预约
        List<AppointmentInfoDTO> appointments =
                appointmentMapper.selectCompletedAppointmentsByPatientId(patientId);

        if (appointments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 一次性查该患者的病例流水（复用旧逻辑）
        List<MedicalRecordDTO> records =
                this.getHistoryByPatientId(patientId, limit);

        // 3. 按 appointmentId 组织病例
        Map<Long, MedicalRecordDTO> recordMap = records.stream()
                .filter(r -> r.getAppointmentId() != null)
                .collect(Collectors.toMap(
                        MedicalRecordDTO::getAppointmentId,
                        r -> r,
                        (a, b) -> a
                ));

        // 4. 组装“就诊记录”
        return appointments.stream().map(appt -> {

            VisitRecordDTO dto = new VisitRecordDTO();
            dto.setAppointmentId(appt.getAppointmentId());
            dto.setDoctorName(appt.getDoctorName());
            dto.setDeptName(appt.getDeptName());
            dto.setAppointmentTime(appt.getAppointmentTime());

            dto.setMedicalRecord(
                    recordMap.get(appt.getAppointmentId())
            );

            return dto;

        }).collect(Collectors.toList());
    }
}
