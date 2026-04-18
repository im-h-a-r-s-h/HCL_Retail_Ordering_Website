package com.retail.retail_app.controller;

import com.retail.retail_app.model.User;
import com.retail.retail_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return service.save(user);
    }
}