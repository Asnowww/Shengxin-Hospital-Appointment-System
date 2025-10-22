package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;       // 状态码，例如200成功，400失败
    private String message; // 提示信息
    private T data;         // 返回数据，可为null

    // 成功返回（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功返回（带数据和消息）
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 失败返回
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }

    // 失败返回（指定状态码）
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
