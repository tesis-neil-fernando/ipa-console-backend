package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{username}")
    public UserEntity getUser(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @PostMapping("")
    public UserEntity createUser(@RequestBody UserEntity user) {
        return userService.createUser(user);
    }

    //editar usuario


}
