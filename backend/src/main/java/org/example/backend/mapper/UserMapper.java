package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.backend.pojo.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("""
        SELECT * FROM users WHERE username = #{username}
    """)
    User findByUsername(@Param("username") String username);

    /**
     * 检查手机号是否已存在
     */
    @Select("""
        SELECT COUNT(*) > 0 FROM users WHERE phone = #{phone}
    """)
    boolean existsByPhone(@Param("phone") String phone);

    /**
     * 插入新用户
     */
    @Insert("""
        INSERT INTO users (username, password, gender, phone, email, role_type, status)
        VALUES (#{username}, #{password}, #{gender}, #{phone}, #{email}, #{roleType}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(User user);

    /**
     * 根据ID查询用户
     */
    @Select("""
        SELECT * FROM users WHERE user_id = #{userId}
    """)
    User selectById(@Param("userId") Long userId);

    /**
     * 根据ID更新用户（仅更新非空字段）
     */
    @Update("""
        UPDATE users
        SET 
            username = COALESCE(#{username}, username),
            password = COALESCE(#{password}, password),
            email = COALESCE(#{email}, email),
            gender = COALESCE(#{gender}, gender),
            status = COALESCE(#{status}, status),
            update_time = #{updateTime}
        WHERE user_id = #{userId}
    """)
    int updateById(User user);

    @Select("""
    SELECT * FROM users WHERE email = #{email}
""")
    User selectByEmail(@Param("email") String email);

    @Update("""
    UPDATE users SET password = #{newPassword}, update_time = NOW()
    WHERE email = #{email}
""")
    int updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);

}
