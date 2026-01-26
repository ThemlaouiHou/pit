package com.pit.web.view;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Application component.
public class RegistrationForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank
    private String confirmPassword;

    // Handles get email request operation
    public String getEmail() {
        return email;
    }

    // Handles set email request operation
    public void setEmail(String email) {
        this.email = email;
    }

    // Handles get password request operation
    public String getPassword() {
        return password;
    }

    // Handles set password request operation
    public void setPassword(String password) {
        this.password = password;
    }

    // Handles get confirm password request operation
    public String getConfirmPassword() {
        return confirmPassword;
    }

    // Handles set confirm password request operation
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // Handles passwords match request operation
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
