package org.example.backend.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户注册或登录时前端提交的参数
 */
@Data
public class UserParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;

    /** 用户真实姓名 */
    private String name;

    /** 性别（M / F） */
    private String gender;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 邮箱验证码（前端注册时填写，用于验证，不入库） */
    private String emailCode;

    /** 角色类型：admin / doctor / patient */
    private String roleType;

    public Object getUserId() {
        return userId;
    }
}
