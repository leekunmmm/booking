package com.luxehaven.booking.dto;

import lombok.Data;

@Data
public class RegisterForm {
    private String username;
    private String password;
    private String confirmPassword;
    private String fullName;
    private String email;
    private String phone;
}
