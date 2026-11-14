package com.university.ums.service;

import com.university.ums.dto.UserRequest;
import com.university.ums.dto.UserResponse;
import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import com.university.ums.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserRepository repository;
    private PasswordEncoder encoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserService(repository, encoder);
        when(encoder.encode("secret123")).thenReturn("hashed-password");
    }

    @Test
    void findAllMapsUsersForRequestedRole() {
        AppUser teacher = user(1L, Role.TEACHER);
        when(repository.findAllByRole(Role.TEACHER)).thenReturn(List.of(teacher));
        assertEquals(List.of(UserResponse.from(teacher)), service.findAll(Role.TEACHER));
    }

    @Test
    void findByIdReturnsMatchingRoleUser() {
        AppUser student = user(2L, Role.STUDENT);
        when(repository.findByIdAndRole(2L, Role.STUDENT)).thenReturn(Optional.of(student));
        assertEquals(UserResponse.from(student), service.findById(2L, Role.STUDENT));
    }

    @Test
    void findByIdThrowsNotFoundWhenMissing() {
        when(repository.findByIdAndRole(9L, Role.ADMIN)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.findById(9L, Role.ADMIN));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void createHashesPasswordAndAssignsRequestedRole() {
        UserRequest request = request();
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        UserResponse result = service.create(request, Role.TEACHER);
        assertEquals(Role.TEACHER, result.role());
        verify(encoder).encode("secret123");
        verify(repository).save(argThat(user -> "hashed-password".equals(user.getPassword())
                && Role.TEACHER.equals(user.getRole()) && "Computer Science".equals(user.getDepartment())));
    }

    @Test
    void createRejectsDuplicateUsernameOrEmail() {
        when(repository.existsByUsername("sam")).thenReturn(true);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(request(), Role.STUDENT));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void updateChangesExistingUserAndAllowsItsOwnUniqueValues() {
        AppUser existing = user(4L, Role.STUDENT);
        UserRequest request = request();
        request.setStudentId("S-100");
        when(repository.findByIdAndRole(4L, Role.STUDENT)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("sam")).thenReturn(Optional.of(existing));
        when(repository.findByEmailIgnoreCase("sam@example.com")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        UserResponse result = service.update(4L, request, Role.STUDENT);
        assertEquals("S-100", result.studentId());
        assertNull(result.department());
        verify(repository).save(existing);
    }

    @Test
    void updateRejectsUsernameOwnedByAnotherUser() {
        when(repository.findByIdAndRole(5L, Role.TEACHER)).thenReturn(Optional.of(user(5L, Role.TEACHER)));
        when(repository.findByUsername("sam")).thenReturn(Optional.of(user(6L, Role.TEACHER)));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.update(5L, request(), Role.TEACHER));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesUserOfRequestedRole() {
        AppUser admin = user(7L, Role.ADMIN);
        when(repository.findByIdAndRole(7L, Role.ADMIN)).thenReturn(Optional.of(admin));
        service.delete(7L, Role.ADMIN);
        verify(repository).delete(admin);
    }

    private static UserRequest request() {
        UserRequest request = new UserRequest();
        request.setUsername("sam");
        request.setPassword("secret123");
        request.setFullName("Sam Student");
        request.setEmail("sam@example.com");
        request.setDepartment("Computer Science");
        return request;
    }

    private static AppUser user(Long id, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("sam");
        user.setPassword("hashed-password");
        user.setFullName("Sam Student");
        user.setEmail("sam@example.com");
        user.setRole(role);
        user.setDepartment(role == Role.STUDENT ? null : "Computer Science");
        user.setStudentId(role == Role.STUDENT ? "S-100" : null);
        return user;
    }
}
