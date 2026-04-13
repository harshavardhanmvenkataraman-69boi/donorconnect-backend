package com.donorconnect.dto.request.auth;
import jakarta.validation.constraints.*;
import lombok.Data;

// Lombok makes the Java file easier for you to write,
// while Jakarta makes the data valid for the Database and the Frontend


@Data // automatically getters,setters,toString(),equals(), hashCode()
public class ResetPasswordRequest {
    @NotBlank private String email;
    private String token;
    @NotBlank @Size(min=6) private String newPassword;
}
