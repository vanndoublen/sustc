package io.pubmed.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContentController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .map(Object::toString)
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        model.addAttribute("userRole", role);
        model.addAttribute("username", auth.getName());

        return "dashboard";  // Single dashboard page with role-based visibility
    }
}