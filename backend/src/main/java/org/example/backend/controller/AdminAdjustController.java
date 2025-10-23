package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.Result;
import org.example.backend.pojo.AppointmentType;
import org.example.backend.service.AppointmentService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/appointment-types")
public class AdminAdjustController {

    @Resource
    private AppointmentService appointmentService;

    /**
     * 获取所有挂号类型
     */
//    @GetMapping
//    public Result<List<AppointmentType>> getAll() {
//        return Result.success(appointmentService.getAllTypes());
//    }

    /**
     * 更新挂号费
     */
    @PutMapping("/{id}/fee")
    public Result<?> updateFee(@PathVariable Integer id, @RequestParam Double fee) {
        boolean ok = appointmentService.updateAppointmentFee(id, fee);
        return ok ? Result.success("更新成功") : Result.error("更新失败，记录不存在");
    }
}
