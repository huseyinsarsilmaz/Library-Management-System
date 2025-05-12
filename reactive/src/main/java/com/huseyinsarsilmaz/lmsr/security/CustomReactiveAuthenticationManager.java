package com.huseyinsarsilmaz.lmsr.security;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.huseyinsarsilmaz.lmsr.service.UserService;

import reactor.core.publisher.Mono;

@Component
public class CustomReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final UserService userService;
    private final JwtService jwtService;

    public CustomReactiveAuthenticationManager(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        String email = jwtService.extractEmail(token);

        return userService.getUserByEmail(email)
                .filter(user -> jwtService.validateToken(token, user.getEmail()))
                .flatMap(user -> {
                    UserDetails userDetails = new User(
                            user.getEmail(),
                            user.getPassword(),
                            Stream.of(user.getRoles().split(","))
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList()));

                    return Mono.just(
                            new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities()));
                });
    }
}
