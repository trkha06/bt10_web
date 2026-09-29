package vn.hcmute.webpr.jwt.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hcmute.webpr.jwt.entity.User;
import vn.hcmute.webpr.jwt.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public List<User> allUsers() { return userService.allUsers(); }

    @GetMapping("/me")
    public User currentUser(@AuthenticationPrincipal User user) { return user; }
}
