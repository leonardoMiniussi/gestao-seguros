package com.gestao.service;

import com.gestao.entity.Usuario;
import com.gestao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - Testes Unitários")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("joao_silva")
                .email("joao@example.com")
                .nome("João Silva")
                .senha("encoded_senha123")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("deve retornar UserDetails quando usuário for encontrado pelo username")
    void loadUserByUsername_usuarioEncontrado_retornaUserDetails() {
        when(usuarioRepository.findByUsername("joao_silva")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("joao_silva");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("joao_silva");
        assertThat(userDetails.getPassword()).isEqualTo("encoded_senha123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("deve retornar UserDetails com enabled=false quando usuário estiver inativo")
    void loadUserByUsername_usuarioInativo_retornaUserDetailsDesabilitado() {
        Usuario usuarioInativo = Usuario.builder()
                .id(2L)
                .username("inativo_user")
                .email("inativo@example.com")
                .nome("Inativo")
                .senha("encoded_senha")
                .ativo(false)
                .build();
        when(usuarioRepository.findByUsername("inativo_user")).thenReturn(Optional.of(usuarioInativo));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("inativo_user");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("deve lançar UsernameNotFoundException quando usuário não for encontrado")
    void loadUserByUsername_usuarioNaoEncontrado_lancaUsernameNotFoundException() {
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("inexistente"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: inexistente");
    }
}

