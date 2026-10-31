package com.university.ums.service;

import com.university.ums.dto.UserRequest;
import com.university.ums.dto.UserResponse;
import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import com.university.ums.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll(Role role) {
        return repository.findAllByRole(role).stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(Long id, Role role) {
        return UserResponse.from(repository.findByIdAndRole(id, role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")));
    }

    public UserResponse create(UserRequest request, Role role) {
        if (repository.existsByUsername(request.getUsername()) || repository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
        }
        AppUser user = new AppUser();
        apply(user, request, role);
        return UserResponse.from(repository.save(user));
    }

    public UserResponse update(Long id, UserRequest request, Role role) {
        AppUser user = repository.findByIdAndRole(id, role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        repository.findByUsername(request.getUsername()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        });
        repository.findByEmailIgnoreCase(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        });
        apply(user, request, role);
        return UserResponse.from(repository.save(user));
    }

    public void delete(Long id, Role role) {
        AppUser user = repository.findByIdAndRole(id, role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        repository.delete(user);
    }

    private void apply(AppUser user, UserRequest request, Role role) {
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setDepartment(role == Role.STUDENT ? null : request.getDepartment());
        user.setStudentId(role == Role.STUDENT ? request.getStudentId() : null);
    }
}
