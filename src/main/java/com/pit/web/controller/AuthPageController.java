package com.pit.web.controller;

import com.pit.service.AuthService;
import com.pit.web.view.RegistrationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthService authService;

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                 BindingResult binding,
                                 RedirectAttributes redirect,
                                 HttpServletRequest request,
                                 Model model) {
        if (!form.passwordsMatch()) {
            binding.rejectValue("confirmPassword", "password.mismatch", "Les mots de passe ne correspondent pas.");
        }
        if (binding.hasErrors()) {
            return "register";
        }
        try {
            authService.registerUser(form.getEmail(), form.getPassword());
        } catch (IllegalArgumentException ex) {
            binding.rejectValue("email", "email.exists", ex.getMessage());
            return "register";
        }

        var authentication = authService.authenticate(form.getEmail(), form.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        redirect.addFlashAttribute("success", "Bienvenue ! Votre compte a été créé.");
        return "redirect:/";
    }
}
