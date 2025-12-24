package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.PatientRegisterParam;
import org.example.backend.pojo.User;

/**
 * 用户业务逻辑接口
 */
public interface UserService extends IService<User> {

    /**
     * 查询用户状态
     */
    String selectStatusByUserId(Long userId);
    /**
     * 注册用户
     * 
     * @param user 用户对象
     * @return 是否注册成功
     */
    boolean register(User user);

    boolean registerPatient(PatientRegisterParam param);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(String email);

    User findByPhone(String phone);

    /**
     *查询用户邮箱
     */
    User getOneByEmail(String email);

    /**
     * 修改密码
     * @param email
     * @param newPassword
     * @return
     */
    boolean updatePasswordByEmail(String email, String newPassword);
}
