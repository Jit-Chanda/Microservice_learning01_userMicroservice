package com.jit.user.service.controllers;

import com.jit.user.service.entities.User;
import com.jit.user.service.services.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //to create a user
    @PostMapping
    public ResponseEntity<User> crateUser(@RequestBody User user) {
        User createdUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    Integer retryCount = 1;
    //to fetch a specific user
    @GetMapping("/{userId}")
    @CircuitBreaker(name = "ratingHotelCircuitBreaker", fallbackMethod = "ratingHotelFallback")
    //@Retry(name = "ratingHotelRetry", fallbackMethod = "ratingHotelFallback")
    @Retry(name = "ratingHotelRetry")
    public ResponseEntity<User> getSingleUser(@PathVariable String userId) {
        log.info("retry count: "+retryCount);
        retryCount++;
        User fetchedUser = userService.getUser(userId);
        return ResponseEntity.ok(fetchedUser);
    }

    //creating fallback method for ratingHotelCircuitBreaker
    //return type should be same as the actual method(in this case it is ResponseEntity<User>)
    //parameters should be same as actual method plus, one exception
    public ResponseEntity<User> ratingHotelFallback(String userId, Exception ex) {
        log.info("Fallback is executed because of: " +ex.getMessage());
        User user = User.builder()
                        .name("dummy")
                        .email("dummy@dummy.com")
                        .about("dummy")
                        .userId("dummy")
                        .build();
        return new ResponseEntity(user, HttpStatus.OK);
    }

    //to fetch all the users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> fetchedUsers = userService.getUsers();
        return ResponseEntity.ok(fetchedUsers);
    }
}
