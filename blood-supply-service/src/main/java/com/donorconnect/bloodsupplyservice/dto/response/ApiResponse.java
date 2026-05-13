package com.donorconnect.bloodsupplyservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String code;
    private Object details;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, "SUCCESS", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, "SUCCESS", null);
    }

    public static <T> ApiResponse<T> error(String message, String code, Object details) {
        return new ApiResponse<>(false, message, null, code, details);
    }
}
