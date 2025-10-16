package org.example.backend.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String password;
    private String name;
    private String gender;
    private String phone;

    private String idCard;

    private String email;
    private String emailCode;
    private String roleType;
}
