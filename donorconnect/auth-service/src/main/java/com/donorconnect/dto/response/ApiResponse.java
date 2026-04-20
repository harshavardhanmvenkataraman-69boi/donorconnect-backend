package com.donorconnect.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ApiResponse<T> {
    private boolean success; // a simple true or false flag
    private String message; // a human-readable explanation(Login successful, Invalid password)
    private T data; // actual payload
    private LocalDateTime timestamp = LocalDateTime.now();
    // Records the exact date and time the response was created


    public static <T> ApiResponse<T> success(String message, T data) { // When: You are "performing an action" (POST, PUT, PATCH)
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true); // true/false
        response.setMessage(message); // custom text (like "Login successful")
        response.setData(data); // places actual data(the User, the Token, or a List) into the "Payload" section
        response.setTimestamp(LocalDateTime.now()); // It stamps the current date and time on the response
        return response;
    }

// dry principle
    public static <T> ApiResponse<T> success(T data) { // best for getMapping
        return success("Success", data);
    } // we call this in getMapping because message is not as important as data



    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
