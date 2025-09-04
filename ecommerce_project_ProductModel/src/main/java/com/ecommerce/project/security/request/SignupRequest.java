package com.ecommerce.project.security.request;


import com.ecommerce.project.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;


@Data
public class SignupRequest {


    @NotBlank
    @Size(min = 5,max = 40)
    private String username ;

    @NotBlank
    @Email
    @Size(min = 5, max = 40)
    private String email;

    private Set<String>role;

    @NotBlank
    @Size(min = 8, max = 40)
    private String password;

    public SignupRequest(String username, String email, Set<String> role, String password) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
    }
}
