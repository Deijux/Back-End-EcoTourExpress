package com.ecotourexpress.ecotourexpress.Auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecotourexpress.ecotourexpress.Jwt.JwtService;
import com.ecotourexpress.ecotourexpress.model.Rol;
import com.ecotourexpress.ecotourexpress.model.User;
import com.ecotourexpress.ecotourexpress.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getContraseña()));
        UserDetails user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.getToken(user);
        return AuthResponse.builder()
            .token(token)
            .build();
    }

    public AuthResponse register(RegisterRequest request, UserDetails currentUser) {
        // Verifica si el usuario actual es admin antes de permitir crear otro admin
        if (request.getRol() == Rol.ROLE_ADMIN && currentUser == null) {
            throw new IllegalStateException("Solo los administradores pueden crear otros administradores.");
        }
    
        // Determinar el rol del usuario, asignar ROLE_USER si no es especificado o si el usuario no es admin
        Rol userRol = (request.getRol() != null && currentUser != null) ? request.getRol() : Rol.ROLE_USER;
    
        User user = User.builder()
            .username(request.getUsername())
            .correo(request.getCorreo())
            .contraseña(passwordEncoder.encode(request.getContraseña()))
            .nombre(request.getNombre())
            .apellido(request.getApellido())
            .rol(userRol)
            .build();
    
        userRepository.save(user);
    
        return AuthResponse.builder()
            .token(jwtService.getToken(user))
            .build();
    }
    
}
