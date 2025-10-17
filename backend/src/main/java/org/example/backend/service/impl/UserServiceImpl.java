package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.PatientRegisterParam;
import org.example.backend.mapper.PatientMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private PatientMapper patientMapper;

    @Override
    public boolean register(User user) {
        // 默认角色
        if (user.getRoleType() == null) {
            user.setRoleType("patient");
        }

        user.setStatus(1);

        return this.save(user);
    }

    @Override
    @Transactional
    public boolean registerPatient(PatientRegisterParam param) {

        // 2. 唯一性检查
        if (findByEmail(param.getEmail()) != null) {
            throw new RuntimeException("该邮箱已被注册，请直接登录");
        }
        if (findByUsername(param.getUsername()) != null) {
            throw new RuntimeException("用户名已存在，请更换");
        }
        if (findByPhone(param.getPhone()) != null) {
            throw new RuntimeException("该手机号已注册");
        }

        // 4. 构建 User 对象
        User user = new User();
        BeanUtils.copyProperties(param, user);
        user.setRoleType("patient");
        user.setStatus(1); // 默认未激活
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(param.getPassword()));

        // 保存 User
        boolean saved = this.save(user);
        if (!saved) {
            throw new RuntimeException("注册用户失败");
        }

        // 5. 构建 Patient 对象
        Patient patient = new Patient();
        BeanUtils.copyProperties(param, patient);
        patient.setUserId(user.getUserId());

        // 保存 Patient
        int insertPatient = patientMapper.insert(patient);
        if (insertPatient <= 0) {
            throw new RuntimeException("注册患者信息失败");
        }

        return true;
    }

    @Override
    public User findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public User findByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    @Override
    public User findByPhone(String phone) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }
}
