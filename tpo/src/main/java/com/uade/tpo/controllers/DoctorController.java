package com.uade.tpo.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.DoctorRequest;
import com.uade.tpo.entity.dto.FilterDoctorRequest;
import com.uade.tpo.entity.enumerations.Role;
import com.uade.tpo.service.implementation.DoctorService;
import com.uade.tpo.service.implementation.UserService;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Void> createDoctors(@RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.ADMIN);
        doctorService.createDoctors();
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteDoctors(@RequestHeader("Authorization") String code){
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.ADMIN);
        doctorService.deleteDoctors();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<DoctorRequest>> searchDoctor(@PathVariable String name,
            @RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        return ResponseEntity.ok(doctorService.searchDoctor(name));
    }
    
    @PostMapping("/filter")
    public ResponseEntity<List<DoctorRequest>> filterDoctors(
            @RequestBody FilterDoctorRequest filterDoctorRequest,
            @RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        return ResponseEntity.ok(doctorService.filterDoctors(filterDoctorRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<List<DoctorRequest>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }
    
    @PostMapping(value = "/image/{doctorId}",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> setImage(@PathVariable Long doctorId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String code) throws SQLException, IOException {
        doctorService.setImage(doctorId, file);
        return ResponseEntity.ok().build();
    }
}
