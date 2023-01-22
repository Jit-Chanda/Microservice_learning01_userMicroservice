package com.jit.user.service.client;

import com.jit.user.service.entities.Rating;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "RATING-SERVICE")
public interface RatingService {

    //I'll not use this class, it's just to understand how we can create different classes for different microservices
    //We can put the return type as ResponseEntity<Rating> or Rating, in case of ResponseEntity<Rating> we will get more info and to
    //get the actual Rating, we need to call getBody(). But, if we keep the return type Rating then, we will get the object directly.
    @PostMapping("/ratings")
    ResponseEntity<Rating> crateRating(@RequestBody Rating rating);
}
