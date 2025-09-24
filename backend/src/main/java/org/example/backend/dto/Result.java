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
}
