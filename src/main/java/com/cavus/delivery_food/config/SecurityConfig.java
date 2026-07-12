package com.cavus.delivery_food.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cavus.delivery_food.auth.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    // ! bunu eklemezsek filter çalışmaz token yok sayılır
    // ! alt tarafta addFilterBefore ile ekleme yapıyoruz
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

   
    // kim hangi endpoint'e erişebilir onu burada belirliyoruz şuan herkese açık halde tutuyoruz

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                    ).permitAll()
                .anyRequest().authenticated())
                // ! brada addFilterBefore diyor ki jwtAuthenticationFilter'ı UsernamePasswordAuthenticationFilter'dan önce çalıştır
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // ! session bilgisini sunucuda saklama demek JWT'li yapıda tercih edilir.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}