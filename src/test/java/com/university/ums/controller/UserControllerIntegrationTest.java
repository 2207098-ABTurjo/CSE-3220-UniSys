package com.university.ums.controller;

import com.university.ums.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ums-integration;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository repository;

    @AfterEach
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateAndReadTeacher() throws Exception {
        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"teacher.one","password":"secret123","fullName":"Taylor Teacher","email":"taylor@example.com","department":"Physics"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/teachers/")))
                .andExpect(jsonPath("$.role").value("TEACHER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("teacher.one"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidCreateRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"123","fullName":"","email":"bad-email"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedApiRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotAccessTeacherEndpoint() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }
}
