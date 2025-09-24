package org.example.backend.service;

import org.example.backend.dto.Result;
import org.example.backend.pojo.User;

public interface LoginService {
    Result<User> login(User user);
}
