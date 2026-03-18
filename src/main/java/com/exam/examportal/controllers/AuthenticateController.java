package com.exam.examportal.controllers;

import com.exam.examportal.config.JwtUtils;
import com.exam.examportal.models.JwtRequest;
import com.exam.examportal.models.JwtResponse;
import com.exam.examportal.models.User;
import com.exam.examportal.services.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AuthenticateController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    // generate token

    @PostMapping(path = "/generate-token")
    public ResponseEntity<?> generateToken(@RequestBody JwtRequest jwtRequest) {

        try {
            this.authenticate(jwtRequest.getUsername(), jwtRequest.getPassword());
        } catch (DisabledException e) {
            return ResponseEntity.status(401).body("User is disabled: " + e.getMessage());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid Credentials: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }

        try {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtRequest.getUsername());
            String token = this.jwtUtils.generateToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (UsernameNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body("User not found");
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new DisabledException("USER DISABLED");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INVALID CREDENTIALS");
        }
    }

    // return details of current user
    @GetMapping(path = "/current-user")
    public User getCurrentUser(Principal principal) {
        return (User) this.userDetailsService.loadUserByUsername(principal.getName());
    }

}
