package org.example.backend.controller;

import org.example.backend.dto.Result;
import org.example.backend.pojo.User;
import org.example.backend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/signin")
    public Result<User> login(@RequestBody User user) {
        return loginService.login(user);
    }
}
