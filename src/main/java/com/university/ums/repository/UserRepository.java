package com.university.ums.repository;

import com.university.ums.model.AppUser;
import com.university.ums.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);
    List<AppUser> findAllByRole(Role role);
    Optional<AppUser> findByIdAndRole(Long id, Role role);
}
