package org.example.backend;

import org.example.backend.dto.PatientUpdateParam;
import org.example.backend.dto.Result;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 患者个人信息功能测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class PatientProfileTest {

    @Autowired
    private PatientService patientService;

    @Test
    public void testGetPatientByUserId() {
        // 这里需要先有一个测试患者数据
        // 实际测试时需要先插入测试数据
        Long testUserId = 1L; // 假设存在用户ID为1的患者

        Patient patient = patientService.getPatientByUserId(testUserId);
        if (patient != null) {
            assertNotNull(patient.getPatientId());
            assertNotNull(patient.getUserId());
        }
    }

    @Test
    public void testUpdatePatientInfo() {
        // 创建测试参数
        PatientUpdateParam param = getUpdateParam();

        // 测试更新（需要先有测试数据）
        Long testUserId = 1L; // 假设存在用户ID为1的患者

        try {
            Result<Void> result = patientService.updatePatientInfo(testUserId, param);
            // 如果用户存在，应该更新成功
            // 如果用户不存在，会抛出异常
        } catch (RuntimeException e) {
            // 预期的异常，如用户不存在等
            assertTrue(e.getMessage().contains("用户不存在") ||
                    e.getMessage().contains("该用户不是患者角色"));
        }
    }

    private static PatientUpdateParam getUpdateParam() {
        PatientUpdateParam param = new PatientUpdateParam();
        //param.setUsername("测试用户");
        param.setGender("M");
        param.setPhone("13800138000");
        param.setEmail("test@example.com");
        param.setPatientAccount("2024001");
        param.setIdentityType("student");
        param.setBirthDate(LocalDate.of(2000, 1, 1));
        param.setAddress("测试地址");
        param.setHeight(new BigDecimal("175.50"));
        param.setWeight(new BigDecimal("70.00"));
        param.setMedicalHistory("无");
        param.setEmergencyContact("紧急联系人");
        param.setEmergencyPhone("13900139000");
        return param;
    }

    @Test
    public void testPatientUpdateParamValidation() {
        PatientUpdateParam param = getPatientUpdateParam();

        // 验证参数设置正确
        //assertEquals("测试用户名", param.getUsername());
        assertEquals("F", param.getGender());
        assertEquals("13700137000", param.getPhone());
        assertEquals("test2@example.com", param.getEmail());
        assertEquals("2024002", param.getPatientAccount());
        assertEquals("teacher", param.getIdentityType());
        assertEquals(LocalDate.of(1990, 5, 15), param.getBirthDate());
        assertEquals("测试地址2", param.getAddress());
        assertEquals(new BigDecimal("165.00"), param.getHeight());
        assertEquals(new BigDecimal("55.00"), param.getWeight());
        assertEquals("无特殊病史", param.getMedicalHistory());
        assertEquals("紧急联系人2", param.getEmergencyContact());
        assertEquals("13600136000", param.getEmergencyPhone());
    }

    private static PatientUpdateParam getPatientUpdateParam() {
        PatientUpdateParam param = new PatientUpdateParam();

        // 测试参数设置
        //param.setUsername("测试用户名");
        param.setGender("F");
        param.setPhone("13700137000");
        param.setEmail("test2@example.com");
        param.setPatientAccount("2024002");
        param.setIdentityType("teacher");
        param.setBirthDate(LocalDate.of(1990, 5, 15));
        param.setAddress("测试地址2");
        param.setHeight(new BigDecimal("165.00"));
        param.setWeight(new BigDecimal("55.00"));
        param.setMedicalHistory("无特殊病史");
        param.setEmergencyContact("紧急联系人2");
        param.setEmergencyPhone("13600136000");
        return param;
    }
}
