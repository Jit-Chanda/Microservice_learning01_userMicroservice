package com.jit.user.service.services.impl;

import com.jit.user.service.client.HotelServiceAPI;
import com.jit.user.service.entities.Hotel;
import com.jit.user.service.entities.Rating;
import com.jit.user.service.entities.User;
import com.jit.user.service.exceptions.ResourceNotFoundException;
import com.jit.user.service.repositories.UserRepository;
import com.jit.user.service.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private RestTemplate restTemplate;

    private HotelServiceAPI hotelServiceAPI;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RestTemplate restTemplate, HotelServiceAPI hotelServiceAPI) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.hotelServiceAPI = hotelServiceAPI;
    }

    @Override
    public User saveUser(User user) {
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(String id) {
        //fetching specific user
        User fetchedUser = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User is not found in the server with id:" + id));
        //fetch ratings of the above user from RATING SERVICE using restTemplate
        Rating[] ratingsOfUser = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+fetchedUser.getUserId(), Rating[].class);
        log.info(String.valueOf(ratingsOfUser));
        //converting the array of ratings into list of ratings
        List<Rating> listOfRatingsOfUser = Arrays.stream(ratingsOfUser).toList();

        List<Rating> ratingListWithHotelDetails = listOfRatingsOfUser.stream().map(
                rating -> {
                    //fetch hotel details from HOTEL SERVICE, for each and every rating which were fetched from RATING SERVICE
                    // here I'm using getForEntity() method(getForObject() method can also be used)
//                    ResponseEntity<Hotel> fetchedHotel = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
                    //getting the hotel object(need to uncomment if we want to use restTemplate)
//                    Hotel hotel = fetchedHotel.getBody();
                    //but, here we are using feign client
                    Hotel hotel = hotelServiceAPI.getHotel(rating.getHotelId());
//                    log.info(String.valueOf(fetchedHotel.getStatusCode()));

                    //set the hotel to rating
                    rating.setHotel(hotel);
                    return rating;
                }
        ).collect(Collectors.toList());


        fetchedUser.setRatings(ratingListWithHotelDetails);


        return fetchedUser;
    }
}
