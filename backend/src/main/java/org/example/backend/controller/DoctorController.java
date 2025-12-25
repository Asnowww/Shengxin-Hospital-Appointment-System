package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.dto.DoctorVO;
import org.example.backend.dto.Result;
import org.example.backend.service.OnlineStatusService;
import org.example.backend.mapper.DoctorBioUpdateRequestMapper;
import org.example.backend.mapper.DoctorMapper;
import org.example.backend.service.DoctorAccountService;
import org.example.backend.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private DoctorService doctorService;

    @Resource
    private DoctorAccountService doctorAccountService;

    @Resource
    private DoctorBioUpdateRequestMapper requestMapper;

    @Resource
    private OnlineStatusService onlineStatusService;

    // 查询所有医生
    @GetMapping("/list")
    public Result<List<DoctorVO>> getAllDoctors() {
        List<DoctorVO> doctors = doctorService.getAllDoctorsWithNameAndDept();
        return Result.success(doctors);
    }

    // 根据 ID 查询医生详情
    @GetMapping("/{id}")
    public DoctorVO getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @GetMapping("/doctorId/{id}")
    public DoctorVO getDoctorByDoctorId(@PathVariable Long id) {
        return doctorService.getDoctorByDoctorId(id);
    }

    /**
     * 新增医生
     */
    @PutMapping
    public Result<String> addDoctor(@RequestBody DoctorVO doctorVO) {
        try {
            doctorService.addDoctor(doctorVO);
            return Result.success("新增医生成功");
        } catch (Exception e) {
            return Result.error("新增医生失败: " + e.getMessage());
        }
    }

    /**
     * 修改医生信息
     * 前端：在医生信息编辑页面，提交修改表单。
     * 请求：PUT /doctors/{id}
     * 参数：医生信息 JSON
     */
    @PutMapping("/{id}")
    public Result<String> updateDoctor(@PathVariable Long id, @RequestBody DoctorVO doctorVO) {
        try {
            doctorVO.setDoctorId(id);
            doctorService.updateDoctorInfo(doctorVO);
            return Result.success("修改成功");
        } catch (RuntimeException e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }
    /**
     * 医生修改自己的信息
     */
    @PutMapping("/{doctorId}/update-contact")
    public Result<String> updateDoctorInfoBySelf(
            @PathVariable Long doctorId,
            @RequestBody DoctorVO doctorVO) {
        try {

            doctorVO.setDoctorId(doctorId);
            doctorService.updateDoctorInfoBySelf(doctorVO);
            return Result.success("修改成功");
        } catch (RuntimeException e) {
            return Result.error("修改失败：" + e.getMessage());
        }
    }


    /**
     * 删除医生
     * 前端：点击医生列表中的“删除”按钮
     * 请求：DELETE /doctor/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDoctor(@PathVariable Integer id) {
        boolean success = doctorService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败：医生不存在");
        }
    }

    /**
     * 根据所属科室id查询医生
     */
    @GetMapping("/dept/{deptId}")
    public Result<List<DoctorVO>> getDoctorsByDept(@PathVariable Integer deptId) {
        if (deptId == null) {
            return Result.error("科室ID不能为空");
        }
        List<DoctorVO> doctors = doctorService.getDoctorVOByDeptId(deptId);
        // 注入实时在线状态
        for (DoctorVO doctor : doctors) {
            boolean online = onlineStatusService.isDoctorOnline(doctor.getDoctorId());
            doctor.setOnlineStatus(online ? "online" : "offline");
        }
        return Result.success(doctors);
    }

    // ---医生端---//
    /**
     * 医生提交修改擅长领域的申请
     */
    @PostMapping("/bio/request")
    public Result<String> submitBioRequest(@RequestParam Long userId, @RequestParam String newBio) {
        try {
            doctorAccountService.submitBioChange(userId, newBio);
            return Result.success("申请提交成功，等待管理员审核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
