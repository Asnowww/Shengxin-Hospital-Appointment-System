package org.example.backend.service;

import jakarta.annotation.Resource;
import org.example.backend.mapper.BookingWarningMapper;
import org.example.backend.pojo.BookingWarning;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 抢号警告服务
 * 使用独立事务确保警告记录即使在主事务回滚时也能保存
 */
@Service
public class BookingWarningService {

    @Resource
    private BookingWarningMapper bookingWarningMapper;

    /**
     * 记录抢号警告（使用独立事务）
     * REQUIRES_NEW: 创建新事务，与调用方法的事务隔离
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWarning(Long patientId, String reason) {
        BookingWarning warning = new BookingWarning();
        warning.setPatientId(patientId);
        warning.setWarningTime(LocalDateTime.now());
        warning.setReason(reason);
        warning.setCreatedAt(LocalDateTime.now());
        bookingWarningMapper.insert(warning);
    }

    /**
     * 统计指定时间内的警告次数
     */
    public Long countWarningsSince(Long patientId, LocalDateTime sinceTime) {
        return bookingWarningMapper.countWarningsSince(patientId, sinceTime);
    }
}