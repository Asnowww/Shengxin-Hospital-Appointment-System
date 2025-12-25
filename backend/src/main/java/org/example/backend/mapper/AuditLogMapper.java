package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.backend.pojo.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    /**
     * 分页查询审计日志
     */
    List<AuditLog> selectPageList(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 统计符合条件的总数
     */
    long countPageList(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
