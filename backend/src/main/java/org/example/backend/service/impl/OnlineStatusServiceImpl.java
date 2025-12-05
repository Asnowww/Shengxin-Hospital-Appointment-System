package org.example.backend.service.impl;

import org.example.backend.service.OnlineStatusService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OnlineStatusServiceImpl implements OnlineStatusService {
    private static final String DOCTOR_KEY_PREFIX = "presence:doctor:";
    private static final String PATIENT_KEY_PREFIX = "presence:patient:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public OnlineStatusServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void markDoctorOnline(Long doctorId) {
        if (doctorId == null) {
            return;
        }
        redisTemplate.opsForValue().set(DOCTOR_KEY_PREFIX + doctorId, "online", DEFAULT_TTL);
    }

    @Override
    public void markDoctorOffline(Long doctorId) {
        if (doctorId == null) {
            return;
        }
        redisTemplate.delete(DOCTOR_KEY_PREFIX + doctorId);
    }

    @Override
    public boolean isDoctorOnline(Long doctorId) {
        if (doctorId == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(DOCTOR_KEY_PREFIX + doctorId));
    }

    @Override
    public void markPatientOnline(Long patientId) {
        if (patientId == null) {
            return;
        }
        redisTemplate.opsForValue().set(PATIENT_KEY_PREFIX + patientId, "online", DEFAULT_TTL);
    }

    @Override
    public void markPatientOffline(Long patientId) {
        if (patientId == null) {
            return;
        }
        redisTemplate.delete(PATIENT_KEY_PREFIX + patientId);
    }

    @Override
    public boolean isPatientOnline(Long patientId) {
        if (patientId == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(PATIENT_KEY_PREFIX + patientId));
    }
}

