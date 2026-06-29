package com.neusoft.cloudbrain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResult<T> {

    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(200, "success", data, LocalDateTime.now());
    }

    public static <T> CommonResult<T> success(String message, T data) {
        return new CommonResult<>(200, message, data, LocalDateTime.now());
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<>(200, "success", null, LocalDateTime.now());
    }

    public static <T> CommonResult<T> fail(int code, String message) {
        return new CommonResult<>(code, message, null, LocalDateTime.now());
    }

    public static <T> CommonResult<T> fail(String message) {
        return new CommonResult<>(500, message, null, LocalDateTime.now());
    }

    public static <T> CommonResult<T> unauthorized(String message) {
        return new CommonResult<>(401, message, null, LocalDateTime.now());
    }

    public static <T> CommonResult<T> forbidden(String message) {
        return new CommonResult<>(403, message, null, LocalDateTime.now());
    }

    public static <T> CommonResult<T> notFound(String message) {
        return new CommonResult<>(404, message, null, LocalDateTime.now());
    }

    public static <T> CommonResult<T> badRequest(String message) {
        return new CommonResult<>(400, message, null, LocalDateTime.now());
    }
}
