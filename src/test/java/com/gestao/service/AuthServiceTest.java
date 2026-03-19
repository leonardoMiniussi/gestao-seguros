package com.gestao.service;

import com.gestao.dto.AuthResponseDTO;
import com.gestao.dto.LoginDTO;
import com.gestao.dto.RegistroDTO;
import com.gestao.entity.Usuario;
import com.gestao.repository.UsuarioRepository;
import com.gestao.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegistroDTO registroDTO;
    private LoginDTO loginDTO;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        registroDTO = RegistroDTO.builder()
                .username("joao_silva")
                .email("joao@example.com")
                .senha("senha123")
                .nome("João Silva")
                .build();

        loginDTO = LoginDTO.builder()
                .username("joao_silva")
                .senha("senha123")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .username("joao_silva")
                .email("joao@example.com")
                .nome("João Silva")
                .senha("encoded_senha123")
                .ativo(true)
                .build();
    }

    // =======================================================================
    // registrar
    // =======================================================================
    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("deve registrar usuário com sucesso e retornar token JWT")
        void comSucesso_retornaAuthResponse() {
            when(usuarioRepository.existsByUsername("joao_silva")).thenReturn(false);
            when(usuarioRepository.existsByEmail("joao@example.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("encoded_senha123");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u = Usuario.builder()
                        .id(1L)
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .nome(u.getNome())
                        .senha(u.getSenha())
                        .ativo(true)
                        .build();
                return u;
            });
            when(jwtTokenProvider.generateToken("joao_silva", 1L)).thenReturn("jwt-token");
            when(jwtTokenProvider.getExpirationTime()).thenReturn(86400L);

            AuthResponseDTO response = authService.registrar(registroDTO);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400L);
            assertThat(response.getUsuarioId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("joao_silva");

            verify(usuarioRepository).save(any(Usuario.class));
            verify(passwordEncoder).encode("senha123");
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando username já estiver em uso")
        void usernameJaEmUso_lancaRuntimeException() {
            when(usuarioRepository.existsByUsername("joao_silva")).thenReturn(true);

            assertThatThrownBy(() -> authService.registrar(registroDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Username já está em uso");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando email já estiver em uso")
        void emailJaEmUso_lancaRuntimeException() {
            when(usuarioRepository.existsByUsername("joao_silva")).thenReturn(false);
            when(usuarioRepository.existsByEmail("joao@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registrar(registroDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email já está em uso");

            verify(usuarioRepository, never()).save(any());
        }
    }

    // =======================================================================
    // login
    // =======================================================================
    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("deve realizar login com sucesso e retornar token JWT")
        void comSucesso_retornaAuthResponse() {
            Authentication authMock = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);
            when(usuarioRepository.findByUsername("joao_silva")).thenReturn(Optional.of(usuario));
            when(jwtTokenProvider.generateToken("joao_silva", 1L)).thenReturn("jwt-token");
            when(jwtTokenProvider.getExpirationTime()).thenReturn(86400L);

            AuthResponseDTO response = authService.login(loginDTO);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(86400L);
            assertThat(response.getUsuarioId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("joao_silva");
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando credenciais forem inválidas")
        void credenciaisInvalidas_lancaRuntimeException() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            assertThatThrownBy(() -> authService.login(loginDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Username ou senha inválidos");
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando usuário não for encontrado após autenticação")
        void usuarioNaoEncontradoAposAutenticacao_lancaRuntimeException() {
            Authentication authMock = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authMock);
            when(usuarioRepository.findByUsername("joao_silva")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Usuário não encontrado");
        }
    }
}


