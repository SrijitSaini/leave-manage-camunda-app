package com.example.workflow.api;

import com.example.workflow.dto.AuthRequest;
import com.example.workflow.security.jwt.JwtUtil;
import com.example.workflow.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", request.getUsername(), e);
            return new ResponseEntity<>("Authentication failed", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }
}
