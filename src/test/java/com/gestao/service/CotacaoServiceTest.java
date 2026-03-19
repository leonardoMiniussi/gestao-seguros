package com.gestao.service;

import com.gestao.dto.CotacaoRequest;
import com.gestao.dto.CotacaoResponse;
import com.gestao.entity.Cotacao;
import com.gestao.entity.Usuario;
import com.gestao.repository.CotacaoRepository;
import com.gestao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CotacaoService - Testes Unitários")
class CotacaoServiceTest {

    @Mock
    private CotacaoRepository cotacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CotacaoService cotacaoService;

    private Usuario usuario;
    private Cotacao cotacaoAtiva;
    private CotacaoRequest request;

    // -----------------------------------------------------------------------
    // Valores esperados para valorEmprestimo=10000, prazoMeses=36
    // premioBruto    = 10000 * 0.02            = 200.00
    // corretagemValor= 200 * 0.05              =  10.00
    // premioTotal    = 200 + 10                = 210.00
    // valorParcelado = 210 / 36 (HALF_UP)      =   5.83
    // -----------------------------------------------------------------------
    private static final BigDecimal VALOR_EMPRESTIMO   = new BigDecimal("10000.00");
    private static final int        PRAZO_MESES        = 36;
    private static final BigDecimal PREMIO_BRUTO_ESP   = new BigDecimal("200.00");
    private static final BigDecimal CORRETAGEM_ESP     = new BigDecimal("10.00");
    private static final BigDecimal PREMIO_TOTAL_ESP   = new BigDecimal("210.00");
    private static final BigDecimal VALOR_PARCELADO_ESP= new BigDecimal("5.83");
    private static final BigDecimal TAXA_PREMIO        = new BigDecimal("0.02");
    private static final BigDecimal TAXA_CORRETAGEM    = new BigDecimal("0.05");

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("joao_silva")
                .email("joao@example.com")
                .nome("João Silva")
                .senha("senha_encoded")
                .build();

        cotacaoAtiva = Cotacao.builder()
                .id(1L)
                .usuario(usuario)
                .valorEmprestimo(VALOR_EMPRESTIMO)
                .prazoMeses(PRAZO_MESES)
                .taxaPremio(TAXA_PREMIO)
                .taxaCorretagem(TAXA_CORRETAGEM)
                .premioBruto(PREMIO_BRUTO_ESP)
                .corretagemValor(CORRETAGEM_ESP)
                .premioTotal(PREMIO_TOTAL_ESP)
                .valorAVista(PREMIO_TOTAL_ESP)
                .valorParcelado(VALOR_PARCELADO_ESP)
                .status("ATIVA")
                .criadaEm(LocalDateTime.now())
                .build();

        request = new CotacaoRequest(VALOR_EMPRESTIMO, PRAZO_MESES);
    }

    // =======================================================================
    // criarCotacao
    // =======================================================================
    @Nested
    @DisplayName("criarCotacao")
    class CriarCotacao {

        @Test
        @DisplayName("deve criar cotação e retornar valores financeiros calculados corretamente")
        void comSucesso_retornaResponseComCalculosCorretos() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
            when(cotacaoRepository.save(any(Cotacao.class))).thenAnswer(inv -> {
                Cotacao c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });

            CotacaoResponse response = cotacaoService.criarCotacao(1L, request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getValorEmprestimo()).isEqualByComparingTo(VALOR_EMPRESTIMO);
            assertThat(response.getPrazoMeses()).isEqualTo(PRAZO_MESES);
            assertThat(response.getTaxaPremio()).isEqualByComparingTo(TAXA_PREMIO);
            assertThat(response.getTaxaCorretagem()).isEqualByComparingTo(TAXA_CORRETAGEM);
            assertThat(response.getPremioBruto()).isEqualByComparingTo(PREMIO_BRUTO_ESP);
            assertThat(response.getCorretagemValor()).isEqualByComparingTo(CORRETAGEM_ESP);
            assertThat(response.getPremioTotal()).isEqualByComparingTo(PREMIO_TOTAL_ESP);
            assertThat(response.getValorAVista()).isEqualByComparingTo(PREMIO_TOTAL_ESP);
            assertThat(response.getValorParcelado()).isEqualByComparingTo(VALOR_PARCELADO_ESP);
            assertThat(response.getStatus()).isEqualTo("ATIVA");

            verify(cotacaoRepository, times(1)).save(any(Cotacao.class));
        }

        @Test
        @DisplayName("deve lançar exception quando usuário não for encontrado")
        void usuarioNaoEncontrado_lancaRuntimeException() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cotacaoService.criarCotacao(99L, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");

            verify(cotacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exception quando valor do empréstimo for zero")
        void valorZero_lancaIllegalArgumentException() {
            CotacaoRequest requestInvalido = new CotacaoRequest(BigDecimal.ZERO, PRAZO_MESES);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> cotacaoService.criarCotacao(1L, requestInvalido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maior que zero");
        }

        @Test
        @DisplayName("deve lançar exception quando prazo for menor que 12 meses")
        void prazoMenorQue12_lancaIllegalArgumentException() {
            CotacaoRequest requestInvalido = new CotacaoRequest(VALOR_EMPRESTIMO, 6);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> cotacaoService.criarCotacao(1L, requestInvalido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("12 meses");
        }
    }

    // =======================================================================
    // listarCotacoesDoUsuario
    // =======================================================================
    @Nested
    @DisplayName("listarCotacoesDoUsuario")
    class ListarCotacoesDoUsuario {

        @Test
        @DisplayName("deve retornar lista com as cotações do usuário")
        void comCotacoes_retornaLista() {
            when(cotacaoRepository.findByUsuarioId(1L)).thenReturn(List.of(cotacaoAtiva));

            List<CotacaoResponse> resultado = cotacaoService.listarCotacoesDoUsuario(1L);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getId()).isEqualTo(1L);
            assertThat(resultado.get(0).getStatus()).isEqualTo("ATIVA");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tiver cotações")
        void semCotacoes_retornaListaVazia() {
            when(cotacaoRepository.findByUsuarioId(1L)).thenReturn(List.of());

            List<CotacaoResponse> resultado = cotacaoService.listarCotacoesDoUsuario(1L);

            assertThat(resultado).isEmpty();
        }
    }

    // =======================================================================
    // obterCotacaoPorId
    // =======================================================================
    @Nested
    @DisplayName("obterCotacaoPorId")
    class ObterCotacaoPorId {

        @Test
        @DisplayName("deve retornar a cotação quando o ID existir")
        void idExistente_retornaCotacao() {
            when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacaoAtiva));

            CotacaoResponse response = cotacaoService.obterCotacaoPorId(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getPremioTotal()).isEqualByComparingTo(PREMIO_TOTAL_ESP);
        }

        @Test
        @DisplayName("deve lançar exception quando o ID não existir")
        void idInexistente_lancaRuntimeException() {
            when(cotacaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cotacaoService.obterCotacaoPorId(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrada");
        }
    }

    // =======================================================================
    // atualizarCotacao
    // =======================================================================
    @Nested
    @DisplayName("atualizarCotacao")
    class AtualizarCotacao {

        // Valores esperados para valorEmprestimo=15000, prazoMeses=48
        // premioBruto    = 15000 * 0.02  = 300.00
        // corretagemValor= 300 * 0.05    =  15.00
        // premioTotal    = 300 + 15      = 315.00
        // valorParcelado = 315 / 48      =   6.56 (HALF_UP)
        private final CotacaoRequest requestAtualizado =
                new CotacaoRequest(new BigDecimal("15000.00"), 48);

        @Test
        @DisplayName("deve atualizar e recalcular os valores financeiros")
        void comSucesso_recalculaERetornaResponse() {
            when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacaoAtiva));
            when(cotacaoRepository.save(any(Cotacao.class))).thenAnswer(inv -> inv.getArgument(0));

            CotacaoResponse response = cotacaoService.atualizarCotacao(1L, requestAtualizado);

            assertThat(response.getValorEmprestimo()).isEqualByComparingTo("15000.00");
            assertThat(response.getPrazoMeses()).isEqualTo(48);
            assertThat(response.getPremioBruto()).isEqualByComparingTo("300.00");
            assertThat(response.getCorretagemValor()).isEqualByComparingTo("15.00");
            assertThat(response.getPremioTotal()).isEqualByComparingTo("315.00");
            assertThat(response.getValorParcelado()).isEqualByComparingTo("6.56");
        }

        @Test
        @DisplayName("deve lançar exception quando cotação não for encontrada")
        void naoEncontrada_lancaRuntimeException() {
            when(cotacaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cotacaoService.atualizarCotacao(99L, requestAtualizado))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrada");
        }

        @Test
        @DisplayName("deve lançar exception quando prazo inválido na atualização")
        void prazoInvalido_lancaIllegalArgumentException() {
            CotacaoRequest requestInvalido = new CotacaoRequest(VALOR_EMPRESTIMO, 6);
            when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacaoAtiva));

            assertThatThrownBy(() -> cotacaoService.atualizarCotacao(1L, requestInvalido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("12 meses");
        }
    }

    // =======================================================================
    // deletarCotacao
    // =======================================================================
    @Nested
    @DisplayName("deletarCotacao")
    class DeletarCotacao {

        @Test
        @DisplayName("deve alterar o status para EXCLUIDA")
        void comSucesso_alteraStatusParaExcluida() {
            when(cotacaoRepository.findById(1L)).thenReturn(Optional.of(cotacaoAtiva));
            when(cotacaoRepository.save(any(Cotacao.class))).thenAnswer(inv -> inv.getArgument(0));

            cotacaoService.deletarCotacao(1L);

            assertThat(cotacaoAtiva.getStatus()).isEqualTo("EXCLUIDA");
            verify(cotacaoRepository, times(1)).save(cotacaoAtiva);
        }

        @Test
        @DisplayName("deve lançar exception quando cotação não for encontrada")
        void naoEncontrada_lancaRuntimeException() {
            when(cotacaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cotacaoService.deletarCotacao(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrada");

            verify(cotacaoRepository, never()).save(any());
        }
    }
}

