package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.User;
import org.example.backend.service.UserService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public boolean register(User user) {
        // 这里可加上密码加密、默认角色设置等逻辑
        if (user.getRoleType() == null) {
            user.setRoleType("patient"); // 默认角色
        }
        user.setStatus(1); // 启用
        return this.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public User findByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
    }
}
