package org.example.backend.service.impl;

import org.example.backend.mapper.UserMapper;
import org.example.backend.dto.Result;
import org.example.backend.pojo.User;
import org.example.backend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Result<User> login(User user) {
        User dbUser = userMapper.findByUsername(user.getUsername());
        if (dbUser == null) {
            return new Result<>(400, "用户不存在", null);
        }

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            return new Result<>(401, "密码错误", null);
        }

        // 登录成功返回用户信息
        return new Result<>(200, "登录成功", dbUser);
    }
}
