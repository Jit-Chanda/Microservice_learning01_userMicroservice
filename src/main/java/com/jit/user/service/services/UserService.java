package com.jit.user.service.services;

import com.jit.user.service.entities.User;

import java.util.List;

public interface UserService {

    //to create user
    User saveUser(User user);

    //to get all user
    List<User> getUsers();

    //get single user by specific userId
    User getUser(String id);
}
