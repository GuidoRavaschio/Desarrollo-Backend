package com.uade.tpo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.RegisterRequest;
import com.uade.tpo.entity.dto.TdRequest;
import com.uade.tpo.entity.dto.UserRequest;
import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.security.AuthenticationResponse;
import com.uade.tpo.service.implementation.UserService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {
    
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<AuthenticationResponse> create(@RequestBody RegisterRequest registerRequest) {
        AuthenticationResponse code = userService.registerUser(registerRequest);
        return ResponseEntity.ok(code);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserRequest userRequest) {
        AuthenticationResponse code = userService.logIn(userRequest);
        return ResponseEntity.ok(code);
    }

    @PostMapping("/insurance/upload")
    public ResponseEntity<Void> uploadInsurance(@RequestBody UserRequest userRequest, @RequestHeader("Authorization") String code) {
        userService.uploadInsurance(code, userRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request-code/{email}")
    public ResponseEntity<Void> requestCode(@PathVariable String email) {
        userService.requestChangePassword(email);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/validate-code")
    public ResponseEntity<Void> validateCode(@RequestBody TdRequest tdRequest) {
        userService.validateCode(tdRequest);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody UserRequest userRequest, 
                @RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userRequest.setEmail(u.getEmail());
        userService.changePassword(userRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/insurance/get")
    public ResponseEntity<UserRequest> getInsurance(@RequestHeader("Authorization") String code) {
        return ResponseEntity.ok(userService.getInsurance(code));
    }
    
    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getCompanies() {
        return ResponseEntity.ok(userService.getCompanies());
    }
    
    
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String code){
        userService.deleteUser(code);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/edit")
    public ResponseEntity<Void> editUser(@RequestHeader("Authorization") String code, @RequestBody UserRequest userRequest) {
        userService.editUser(code, userRequest);
        return ResponseEntity.ok().build();
    }
}
