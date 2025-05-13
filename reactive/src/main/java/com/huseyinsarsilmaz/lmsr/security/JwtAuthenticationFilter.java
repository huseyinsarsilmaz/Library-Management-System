package com.huseyinsarsilmaz.lmsr.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            String username = jwtService.extractEmail(jwtToken);

            if (username != null) {
                return Mono.just(jwtService.validateToken(jwtToken, username))
                        .flatMap(valid -> {
                            if (valid) {
                                List<String> roles = jwtService.extractRoles(jwtToken);

                                List<GrantedAuthority> authorities = roles.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());

                                Authentication auth = new UsernamePasswordAuthenticationToken(username, null,
                                        authorities);

                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                            }

                            return chain.filter(exchange);
                        });
            }
        }

        return chain.filter(exchange);
    }

}
