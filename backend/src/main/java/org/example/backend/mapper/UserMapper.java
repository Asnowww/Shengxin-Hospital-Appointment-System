package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.pojo.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User findByUsername(String username);
}
