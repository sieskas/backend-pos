package com.rotules.backend.api.v1.controller;

import com.rotules.backend.api.v1.controller.resources.auth.ContactDTO;
import com.rotules.backend.api.v1.controller.resources.auth.LoginRequest;
import com.rotules.backend.api.v1.controller.resources.auth.PersonDTO;
import com.rotules.backend.api.v1.controller.resources.auth.UserDTO;
import com.rotules.backend.domain.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            User user = (User) authentication.getPrincipal();
            UserDTO userDTO = new UserDTO(
                    user.getUsername(),
                    user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()),
                    new PersonDTO(
                            user.getPerson() != null ? user.getPerson().getFirstName() : null,
                            user.getPerson() != null ? user.getPerson().getLastName() : null,
                            user.getPerson() != null && user.getPerson().getContact() != null
                                    ? new ContactDTO(user.getPerson().getContact().getEmail())
                                    : null
                    )
            );
            return ResponseEntity.ok(userDTO);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Déconnexion réussie");
    }
}
