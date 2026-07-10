package com.cavus.delivery_food.auth.service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;


import com.cavus.delivery_food.auth.dto.LoginRequest;
import com.cavus.delivery_food.auth.dto.LoginResponse;
import com.cavus.delivery_food.auth.dto.RegisterRequest;
import com.cavus.delivery_food.auth.dto.RegisterResponse;
import com.cavus.delivery_food.auth.entity.Role;
import com.cavus.delivery_food.auth.entity.User;
import com.cavus.delivery_food.auth.mapper.UserMapper;
import com.cavus.delivery_food.auth.repository.AuthRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AuthRepository authRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    AuthService(AuthRepository authRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authRepository = authRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if(authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }


        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        

       return userMapper.toRegisterResponse(authRepository.save(user));

    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {

        // 1. Spring Security'ye devret: user bul + şifre doğrula
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // 2. Principal = CustomUserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

         // 3. JWT üret
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token, "Bearer");


    }
    
}
