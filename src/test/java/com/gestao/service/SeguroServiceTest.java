package com.gestao.service;

import com.gestao.dto.SeguroResponseDTO;
import com.gestao.entity.Cotacao;
import com.gestao.entity.Seguro;
import com.gestao.entity.Usuario;
import com.gestao.repository.CotacaoRepository;
import com.gestao.repository.SeguroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeguroServiceTest {

    @Mock
    private SeguroRepository seguroRepository;

    @Mock
    private CotacaoRepository cotacaoRepository;

    @InjectMocks
    private SeguroService seguroService;

    private Usuario usuario;
    private Cotacao cotacao;

    @BeforeEach
    public void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("joao_silva")
                .email("joao@example.com")
                .nome("João Silva")
                .senha("senha123")
                .build();

        cotacao = Cotacao.builder()
                .id(1L)
                .usuario(usuario)
                .valorEmprestimo(new BigDecimal("10000.00"))
                .prazoMeses(36)
                .taxaPremio(new BigDecimal("0.02"))
                .taxaCorretagem(new BigDecimal("0.05"))
                .premioBruto(new BigDecimal("2.00"))
                .corretagemValor(new BigDecimal("0.10"))
                .premioTotal(new BigDecimal("2.10"))
                .valorAVista(new BigDecimal("2.10"))
                .valorParcelado(new BigDecimal("0.06"))
                .status("ATIVA")
                .build();
    }

    @Test
    public void testAprovarCotacaoComSucesso() {
        // Arrange
        Seguro seguroMock = Seguro.builder()
                .id(1L)
                .usuario(usuario)
                .cotacao(cotacao)
                .valorEmprestimo(cotacao.getValorEmprestimo())
                .prazoMeses(cotacao.getPrazoMeses())
                .taxaPremio(cotacao.getTaxaPremio())
                .taxaCorretagem(cotacao.getTaxaCorretagem())
                .premioBruto(cotacao.getPremioBruto())
                .corretagemValor(cotacao.getCorretagemValor())
                .premioTotal(cotacao.getPremioTotal())
                .valorAVista(cotacao.getValorAVista())
                .valorParcelado(cotacao.getValorParcelado())
                .status("ATIVO")
                .build();

        when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacao));
        when(seguroRepository.save(any(Seguro.class))).thenReturn(seguroMock);
        when(cotacaoRepository.save(any(Cotacao.class))).thenReturn(cotacao);

        // Act
        SeguroResponseDTO resultado = seguroService.aprovarCotacao(1L, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(1L, resultado.getCotacaoId());
        assertEquals(new BigDecimal("2.00"), resultado.getPremioBruto());
        assertEquals(new BigDecimal("0.10"), resultado.getCorretagemValor());
        assertEquals(new BigDecimal("2.10"), resultado.getPremioTotal());
        assertEquals(new BigDecimal("0.06"), resultado.getValorParcelado());
        assertEquals("ATIVO", resultado.getStatus());

        verify(seguroRepository, times(1)).save(any(Seguro.class));
        verify(cotacaoRepository, times(1)).save(any(Cotacao.class));
        assertEquals("APROVADA", cotacao.getStatus());
    }

    @Test
    public void testAprovarCotacaoNaoEncontrada() {
        // Arrange
        when(cotacaoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                seguroService.aprovarCotacao(99L, 1L));
        assertTrue(ex.getMessage().contains("não encontrada"));
    }

    @Test
    public void testAprovarCotacaoDeOutroUsuario() {
        // Arrange
        when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacao));

        // Act & Assert — usuário ID 99 não é o dono da cotação (dono é ID 1)
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                seguroService.aprovarCotacao(1L, 99L));
        assertTrue(ex.getMessage().contains("não pertence"));
    }

    @Test
    public void testAprovarCotacaoJaAprovada() {
        // Arrange
        cotacao.setStatus("APROVADA");
        when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacao));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                seguroService.aprovarCotacao(1L, 1L));
        assertTrue(ex.getMessage().contains("não pode ser aprovada"));
    }

    @Test
    public void testListarSegurosDoUsuario() {
        // Arrange
        Seguro seguro = Seguro.builder()
                .id(1L)
                .usuario(usuario)
                .cotacao(cotacao)
                .valorEmprestimo(new BigDecimal("10000.00"))
                .prazoMeses(36)
                .taxaPremio(new BigDecimal("0.02"))
                .taxaCorretagem(new BigDecimal("0.05"))
                .premioBruto(new BigDecimal("2.00"))
                .corretagemValor(new BigDecimal("0.10"))
                .premioTotal(new BigDecimal("2.10"))
                .valorAVista(new BigDecimal("2.10"))
                .valorParcelado(new BigDecimal("0.06"))
                .status("ATIVO")
                .build();

        when(seguroRepository.findByUsuarioId(1L)).thenReturn(List.of(seguro));

        // Act
        List<SeguroResponseDTO> resultado = seguroService.listarSegurosDoUsuario(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("ATIVO", resultado.get(0).getStatus());
    }
}
