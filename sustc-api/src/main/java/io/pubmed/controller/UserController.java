package io.pubmed.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user/info")
    public Map<String, Object> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", auth.getName());
        userInfo.put("roles", Collections.singletonList(role));
        return userInfo;
    }
}
