package org.example.backend.service;

import org.example.backend.dto.MedicalRecordDTO;
import org.example.backend.dto.MedicalRecordParam;

import java.util.List;

public interface MedicalRecordService {

    MedicalRecordDTO getLatestByPatientId(Long patientId);

    List<MedicalRecordDTO> getHistoryByPatientId(Long patientId, Integer limit);

    MedicalRecordDTO createRecord(MedicalRecordParam param);

    MedicalRecordDTO updateRecord(Long recordId, MedicalRecordParam param);
}
