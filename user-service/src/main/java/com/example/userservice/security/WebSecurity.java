package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity  {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment environment;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = getAuthenticationFilter(http);


        http.csrf( (csrf) -> csrf.disable());


        http.authorizeHttpRequests((authz) ->
                        authz.requestMatchers("/**")
                        .access(new WebExpressionAuthorizationManager(" hasIpAddress('127.0.0.1') or hasIpAddress('192.168.0.3')")) // host pc ip address
                        .anyRequest()
                        .authenticated()
                )
                .authenticationManager(authenticationManager)
                .addFilter(getAuthenticationFilter());


        http.headers((headers) ->
                headers.frameOptions((frameOptions) -> frameOptions.sameOrigin())
        );

        return http.build();
    }
    private AuthenticationManager getAuthenticationFilter(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(userService, environment);
        filter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        return filter;
    }

}
