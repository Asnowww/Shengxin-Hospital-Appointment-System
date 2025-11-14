package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.dto.Result;
import org.example.backend.pojo.AppointmentType;
import org.example.backend.service.AppointmentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/appointment-types")
public class AppointmentTypeController {

    @Autowired
    private AppointmentTypeService appointmentTypeService;

    /**
     * 获取全部号别（支持分页 + 模糊搜索）
     */
    @GetMapping("/list")
    public
    Result<List<AppointmentType>> list(
            @RequestParam(required = false) String keyword
    ) {
        LambdaQueryWrapper<AppointmentType> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(AppointmentType::getTypeName, keyword)
                    .or()
                    .like(AppointmentType::getTypeKey, keyword);
        }
        List<AppointmentType> list = appointmentTypeService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 获取单个号别详情
     */
    @GetMapping("/{id}")
    public Result<AppointmentType> getById(@PathVariable Integer id) {
        AppointmentType appointmentType = appointmentTypeService.getById(id);
        if (appointmentType == null) {
            return Result.error(404,"未找到该号别");
        }
        return Result.success(appointmentType);
    }

    /**
     * 新增号别
     */
    @PostMapping
    public Result<AppointmentType> create(@RequestBody AppointmentType appointmentType) {
        if (appointmentType.getTypeName() == null || appointmentType.getTypeName().isEmpty()) {
            return Result.error(400, "typeName 不能为空");
        }
        if (appointmentType.getMaxSlots() == null || appointmentType.getMaxSlots() <= 0) {
            appointmentType.setMaxSlots(10); // 默认值
        }

        try {
            boolean saved = appointmentTypeService.save(appointmentType);
            if (saved) {
                return Result.success("创建成功", appointmentType);
            } else {
                return Result.error(500, "创建失败");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 检查是否是唯一约束冲突
            if (e.getCause() != null && e.getCause().getMessage().contains("Duplicate")) {
                return Result.error(409, "数据已存在，请勿重复添加"); // 409 表示冲突
            }
            return Result.error(500, "数据库异常: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "服务器异常: " + e.getMessage());
        }
    }


    /**
     * 修改号别
     */
    @PutMapping("/{id}")
    public Result<AppointmentType> update(@PathVariable Integer id, @RequestBody AppointmentType appointmentType) {
        // 基本校验
        if (appointmentType.getTypeName() == null || appointmentType.getTypeName().isEmpty()) {
            return Result.error(400, "typeName 不能为空");
        }

        // 先查询原始记录
        AppointmentType exist = appointmentTypeService.getById(id);
        if (exist == null) {
            return Result.error(404, "号别不存在");
        }

        // 对比关键字段：typeKey, typeName, feeAmount, description
        boolean same =
                Objects.equals(exist.getTypeKey(), appointmentType.getTypeKey()) &&
                        Objects.equals(exist.getTypeName(), appointmentType.getTypeName()) &&
                        (exist.getFeeAmount() == null
                                ? appointmentType.getFeeAmount() == null
                                : exist.getFeeAmount().compareTo(appointmentType.getFeeAmount()) == 0) &&
                        Objects.equals(
                                exist.getDescription() == null ? "" : exist.getDescription(),
                                appointmentType.getDescription() == null ? "" : appointmentType.getDescription()
                        )&&
                        Objects.equals(exist.getMaxSlots(), appointmentType.getMaxSlots());

        if (same) {
            return Result.error("数据未改变");
        }

        // 准备更新实体（保证 id 正确）
        appointmentType.setAppointmentTypeId(id);

        try {
            boolean updated = appointmentTypeService.updateById(appointmentType);
            if (updated) {
                // 为了确保 createdAt/updatedAt 被正确回填，从数据库重新查询并返回
                AppointmentType updatedEntity = appointmentTypeService.getById(id);
                return Result.success("修改成功", updatedEntity);
            } else {
                return Result.error(500, "修改失败");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 唯一约束冲突处理（更安全地获取 root cause）
            Throwable root = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(e);
            String msg = root != null ? root.getMessage() : e.getMessage();
            if (msg != null && msg.toLowerCase().contains("duplicate")) {
                return Result.error(409, "数据已存在（唯一约束冲突），请检查 typeKey 或 typeName");
            }
            return Result.error(500, "数据库异常: " + msg);
        } catch (Exception e) {
            return Result.error(500, "服务器异常: " + e.getMessage());
        }
    }


    /**
     * 删除号别
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        try {
            boolean removed = appointmentTypeService.removeById(id);
            if (removed) {
                return Result.success("删除成功", null);
            } else {
                return Result.error(404, "号别不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error(500, "服务器异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "删除列表不能为空");
        }
        try {
            boolean removed = appointmentTypeService.removeBatchByIds(ids);
            if (removed) {
                return Result.success("批量删除成功", null);
            } else {
                return Result.error(404, "部分号别不存在或删除失败");
            }
        } catch (Exception e) {
            return Result.error(500, "服务器异常: " + e.getMessage());
        }
    }
}
