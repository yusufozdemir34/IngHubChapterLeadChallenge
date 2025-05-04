package com.github.yozdemir.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Login request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @Size(min = 3, max = 20, message = "{validation.user.username.length}")
    @NotBlank(message = "{validation.user.username.required}")
    private String username;

    @Size(min = 6, max = 100, message = "{validation.user.password.length}")
    @NotBlank(message = "{validation.user.password.required}")
    private String password;
}
