package com.university.ums.dto;

import com.university.ums.model.AppUser;
import com.university.ums.model.Role;

public record UserResponse(Long id, String username, String fullName, String email,
                           Role role, String department, String studentId) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getRole(), user.getDepartment(), user.getStudentId());
    }
}
