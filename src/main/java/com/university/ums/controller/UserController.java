package com.university.ums.controller;

import com.university.ums.dto.UserRequest;
import com.university.ums.dto.UserResponse;
import com.university.ums.model.Role;
import com.university.ums.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/api/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAdmins() { return service.findAll(Role.ADMIN); }

    @GetMapping("/api/admins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getAdmin(@PathVariable Long id) { return service.findById(id, Role.ADMIN); }

    @PostMapping("/api/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody UserRequest request) {
        UserResponse user = service.create(request, Role.ADMIN);
        return ResponseEntity.created(URI.create("/api/admins/" + user.id())).body(user);
    }

    @PutMapping("/api/admins/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateAdmin(@PathVariable Long id, @Valid @RequestBody UserRequest request) { return service.update(id, request, Role.ADMIN); }

    @DeleteMapping("/api/admins/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAdmin(@PathVariable Long id) { service.delete(id, Role.ADMIN); }

    @GetMapping("/api/teachers")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<UserResponse> getTeachers() { return service.findAll(Role.TEACHER); }

    @GetMapping("/api/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public UserResponse getTeacher(@PathVariable Long id) { return service.findById(id, Role.TEACHER); }

    @PostMapping("/api/teachers")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<UserResponse> createTeacher(@Valid @RequestBody UserRequest request) {
        UserResponse user = service.create(request, Role.TEACHER);
        return ResponseEntity.created(URI.create("/api/teachers/" + user.id())).body(user);
    }

    @PutMapping("/api/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public UserResponse updateTeacher(@PathVariable Long id, @Valid @RequestBody UserRequest request) { return service.update(id, request, Role.TEACHER); }

    @DeleteMapping("/api/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public void deleteTeacher(@PathVariable Long id) { service.delete(id, Role.TEACHER); }

    @GetMapping("/api/students")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public List<UserResponse> getStudents() { return service.findAll(Role.STUDENT); }

    @GetMapping("/api/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public UserResponse getStudent(@PathVariable Long id) { return service.findById(id, Role.STUDENT); }

    @PostMapping("/api/students")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public ResponseEntity<UserResponse> createStudent(@Valid @RequestBody UserRequest request) {
        UserResponse user = service.create(request, Role.STUDENT);
        return ResponseEntity.created(URI.create("/api/students/" + user.id())).body(user);
    }

    @PutMapping("/api/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public UserResponse updateStudent(@PathVariable Long id, @Valid @RequestBody UserRequest request) { return service.update(id, request, Role.STUDENT); }

    @DeleteMapping("/api/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    public void deleteStudent(@PathVariable Long id) { service.delete(id, Role.STUDENT); }
}
