package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.WaitlistCreateParam;
import org.example.backend.dto.WaitlistDetailVO;
import org.example.backend.pojo.Waitlist;

import java.util.List;

public interface WaitlistService extends IService<Waitlist> {

    /**
     * 创建候补预约
     * @param param 候补创建参数
     * @return 创建的候补记录
     */
    Waitlist createWaitlist(WaitlistCreateParam param);

    /**
     * 取消候补
     * @param waitId 候补ID
     * @param patientId 患者ID（用于验证权限）
     * @return 是否取消成功
     */
    boolean cancelWaitlist(Long waitId, Long patientId);

    /**
     * 获取患者的候补详细列表（包含排班信息和队列统计）
     */
    List<WaitlistDetailVO> getPatientWaitlistsDetail(Long patientId);

    /**
     * 处理排班释放号源时的候补转正
     * 当有人取消预约或管理员加号时调用
     * @param scheduleId 排班ID
     */
    void processWaitlistConversion(Integer scheduleId);

    /**
     * 检查某个排班是否有候补队列
     * @param scheduleId 排班ID
     * @return 是否有候补
     */
    boolean hasWaitlist(Integer scheduleId);
}