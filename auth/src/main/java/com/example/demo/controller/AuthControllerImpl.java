package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.CredentialsDTO;
import com.example.demo.model.Role;
import com.example.demo.service.UserService;

@RestController
public class AuthControllerImpl implements AuthController{

    @Autowired
    private UserService userService;

    @GetMapping("/publicKey")
    public ResponseEntity<String> getPublicKey(){
        return ResponseEntity.ok(userService.getPublicKey());
    }

    
    @PostMapping("/login")
    public ResponseEntity<String> getJwt(@RequestBody CredentialsDTO loginRequest){
        String isAuthenticated = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
        if(isAuthenticated.isBlank()){
            return new ResponseEntity<String>("user not authenticated", null, 403);
        }
        return new ResponseEntity<String>(isAuthenticated, null, 200);
    }

    @PostMapping("/create")
    public ResponseEntity<Boolean> createUser(@RequestParam String username, @RequestParam String email, @RequestParam String password){

        userService.createUser(username, email, password, Role.USER);
        return new ResponseEntity<Boolean>(true, null, 200);
    }
}
