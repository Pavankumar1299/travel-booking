package com.example.travel_booking.controller;

import com.example.travel_booking.entity.User;
import com.example.travel_booking.repository.UserRepository;
import com.example.travel_booking.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        // check email exist or not
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Email already exists!";
        }

        // encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // save user
        userRepository.save(user);

        return "User registered successfully!";
    }

    
    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (user == null) {
            return "User not found!";
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return "Invalid password!";
        }

        // create token
        String token = jwtUtil.generateToken(user.getEmail());

        return token;
    }

}



