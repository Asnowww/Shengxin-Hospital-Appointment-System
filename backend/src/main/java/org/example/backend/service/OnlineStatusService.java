package org.example.backend.service;

public interface OnlineStatusService {
    void markDoctorOnline(Long doctorId);

    void markDoctorOffline(Long doctorId);

    boolean isDoctorOnline(Long doctorId);

    void markPatientOnline(Long patientId);

    void markPatientOffline(Long patientId);

    boolean isPatientOnline(Long patientId);
}

