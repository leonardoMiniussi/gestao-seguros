package com.gestao.service;

import com.gestao.dto.RegistroDTO;
import com.gestao.dto.LoginDTO;
import com.gestao.dto.AuthResponseDTO;
import com.gestao.entity.Usuario;
import com.gestao.repository.UsuarioRepository;
import com.gestao.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO registrar(RegistroDTO registroDTO) {
        log.info("Registrando novo usuário: {}", registroDTO.getUsername());

        // Verificar se usuário já existe
        if (usuarioRepository.existsByUsername(registroDTO.getUsername())) {
            throw new RuntimeException("Username já está em uso");
        }

        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }

        // Criar novo usuário
        Usuario usuario = Usuario.builder()
            .username(registroDTO.getUsername())
            .email(registroDTO.getEmail())
            .nome(registroDTO.getNome())
            .senha(passwordEncoder.encode(registroDTO.getSenha()))
            .ativo(true)
            .build();

        Usuario savedUsuario = usuarioRepository.save(usuario);
        log.info("Usuário registrado com sucesso: {}", savedUsuario.getId());

        // Gerar token JWT
        String token = jwtTokenProvider.generateToken(savedUsuario.getUsername(), savedUsuario.getId());

        return AuthResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpirationTime())
            .usuarioId(savedUsuario.getId())
            .username(savedUsuario.getUsername())
            .build();
    }

    public AuthResponseDTO login(LoginDTO loginDTO) {
        log.info("Login do usuário: {}", loginDTO.getUsername());

        try {
            // Autenticar
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getSenha()
                )
            );

            // Obter usuário
            Usuario usuario = usuarioRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Gerar token JWT
            String token = jwtTokenProvider.generateToken(usuario.getUsername(), usuario.getId());

            log.info("Login bem-sucedido: {}", loginDTO.getUsername());

            return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .usuarioId(usuario.getId())
                .username(usuario.getUsername())
                .build();

        } catch (AuthenticationException e) {
            log.error("Falha na autenticação: {}", loginDTO.getUsername());
            throw new RuntimeException("Username ou senha inválidos");
        }
    }
}

