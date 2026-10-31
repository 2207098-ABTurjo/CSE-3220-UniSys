package com.university.ums.controller;

import com.university.ums.dto.UserRequest;
import com.university.ums.dto.UserResponse;
import com.university.ums.model.Role;
import com.university.ums.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {
    private static final Map<String, Resource> RESOURCES = Map.of(
            "admins", new Resource(Role.ADMIN, "Administrators"),
            "teachers", new Resource(Role.TEACHER, "Teachers"),
            "students", new Resource(Role.STUDENT, "Students")
    );

    private final UserService userService;

    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Role role = currentRole(authentication);
        List<ResourceCard> cards = new ArrayList<>();
        for (Role resourceRole : Role.values()) {
            String path = resourcePath(resourceRole);
            Resource resource = RESOURCES.get(path);
            if (canManage(role, resourceRole)) {
                cards.add(new ResourceCard(resource.label(), path,
                        userService.findAll(resourceRole).size()));
            }
        }
        model.addAttribute("cards", cards);
        addUserContext(model, authentication);
        return "dashboard";
    }

    @GetMapping("/users/{resource}")
    public String users(@PathVariable String resource, Authentication authentication, Model model) {
        Resource selected = authorizedResource(resource, authentication);
        model.addAttribute("users", userService.findAll(selected.role()));
        model.addAttribute("resource", resource);
        model.addAttribute("resourceLabel", selected.label());
        addUserContext(model, authentication);
        return "users";
    }

    @GetMapping("/users/{resource}/new")
    public String newUser(@PathVariable String resource, Authentication authentication, Model model) {
        Resource selected = authorizedResource(resource, authentication);
        model.addAttribute("user", new UserRequest());
        model.addAttribute("resource", resource);
        model.addAttribute("resourceLabel", selected.label());
        model.addAttribute("formTitle", "Add " + selected.singular());
        model.addAttribute("formAction", "/users/" + resource);
        model.addAttribute("editing", false);
        addUserContext(model, authentication);
        return "user-form";
    }

    @GetMapping("/users/{resource}/{id}/edit")
    public String editUser(@PathVariable String resource, @PathVariable Long id,
                           Authentication authentication, Model model) {
        Resource selected = authorizedResource(resource, authentication);
        UserResponse existing = userService.findById(id, selected.role());
        UserRequest user = new UserRequest();
        user.setUsername(existing.username());
        user.setFullName(existing.fullName());
        user.setEmail(existing.email());
        user.setDepartment(existing.department());
        user.setStudentId(existing.studentId());
        populateForm(model, resource, selected, user, "Edit " + selected.singular(),
                "/users/" + resource + "/" + id, true, authentication);
        return "user-form";
    }

    @PostMapping("/users/{resource}")
    public String createUser(@PathVariable String resource, @Valid @ModelAttribute("user") UserRequest user,
                             BindingResult bindingResult, Authentication authentication, Model model) {
        Resource selected = authorizedResource(resource, authentication);
        if (bindingResult.hasErrors()) {
            showFormErrors(bindingResult, model);
            populateForm(model, resource, selected, user, "Add " + selected.singular(),
                    "/users/" + resource, false, authentication);
            return "user-form";
        }
        try {
            userService.create(user, selected.role());
            return "redirect:/users/" + resource;
        } catch (ResponseStatusException exception) {
            model.addAttribute("formError", exception.getReason());
            populateForm(model, resource, selected, user, "Add " + selected.singular(),
                    "/users/" + resource, false, authentication);
            return "user-form";
        }
    }

    @PostMapping("/users/{resource}/{id}")
    public String updateUser(@PathVariable String resource, @PathVariable Long id,
                             @Valid @ModelAttribute("user") UserRequest user,
                             BindingResult bindingResult, Authentication authentication, Model model) {
        Resource selected = authorizedResource(resource, authentication);
        if (bindingResult.hasErrors()) {
            showFormErrors(bindingResult, model);
            populateForm(model, resource, selected, user, "Edit " + selected.singular(),
                    "/users/" + resource + "/" + id, true, authentication);
            return "user-form";
        }
        try {
            userService.update(id, user, selected.role());
            return "redirect:/users/" + resource;
        } catch (ResponseStatusException exception) {
            model.addAttribute("formError", exception.getReason());
            populateForm(model, resource, selected, user, "Edit " + selected.singular(),
                    "/users/" + resource + "/" + id, true, authentication);
            return "user-form";
        }
    }

    @PostMapping("/users/{resource}/{id}/delete")
    public String deleteUser(@PathVariable String resource, @PathVariable Long id,
                             Authentication authentication) {
        Resource selected = authorizedResource(resource, authentication);
        userService.delete(id, selected.role());
        return "redirect:/users/" + resource;
    }

    private Resource authorizedResource(String resource, Authentication authentication) {
        Resource selected = RESOURCES.get(resource);
        if (selected == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        if (!canManage(currentRole(authentication), selected.role())) {
            throw new AccessDeniedException("You do not have permission to manage this resource");
        }
        return selected;
    }

    private boolean canManage(Role currentRole, Role resourceRole) {
        return currentRole == Role.ADMIN || currentRole == resourceRole;
    }

    private String resourcePath(Role role) {
        return switch (role) {
            case ADMIN -> "admins";
            case TEACHER -> "teachers";
            case STUDENT -> "students";
        };
    }

    private Role currentRole(Authentication authentication) {
        return Role.valueOf(authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("No application role assigned"))
                .substring("ROLE_".length()));
    }

    private void addUserContext(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roleCode", currentRole(authentication).name());
    }

    private void populateForm(Model model, String resource, Resource selected, UserRequest user,
                              String title, String action, boolean editing, Authentication authentication) {
        model.addAttribute("user", user);
        model.addAttribute("resource", resource);
        model.addAttribute("resourceLabel", selected.label());
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("editing", editing);
        addUserContext(model, authentication);
    }

    private void showFormErrors(BindingResult bindingResult, Model model) {
        model.addAttribute("validationErrors", bindingResult.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .toList());
    }

    private record Resource(Role role, String label) {
        private String singular() {
            return switch (role) {
                case ADMIN -> "administrator";
                case TEACHER -> "teacher";
                case STUDENT -> "student";
            };
        }
    }

    public record ResourceCard(String label, String path, int count) {}
}
