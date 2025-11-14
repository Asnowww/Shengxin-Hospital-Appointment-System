package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.pojo.AppointmentType;
import org.example.backend.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fee")
public class FeeController {
    @Resource
    private AppointmentService appointmentTypeService;

    @Resource
    private AppointmentService appointmentService;

    /**
     * 根据挂号ID计算费用并写回数据库
     */
    @PostMapping("/{appointmentId}")
    public Result<Object> calculateFee(@PathVariable Long appointmentId) {
        return appointmentService.calculateFee(appointmentId);
    }

    /**
     * 获取相关费用
     */
    @GetMapping("/type_amount")
    public Result<List<Map<String, Object>>> getAllAppointmentTypes() {
        try {
            List<AppointmentType> list = appointmentTypeService.getAllAppointmentTypes();

            // 只返回前端关心的字段
            List<Map<String, Object>> result = list.stream()
                    .map(type -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("appointmentTypeId", type.getAppointmentTypeId());
                        map.put("appointmentType", type.getTypeName());
                        map.put("fee", type.getFeeAmount());
                        return map;
                    })
                    .collect(Collectors.toList());

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取预约类型失败: " + e.getMessage());
        }
    }
}
