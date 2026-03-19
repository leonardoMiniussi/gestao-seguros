package com.gestao.service;

import com.gestao.dto.CotacaoRequest;
import com.gestao.dto.CotacaoResponse;
import com.gestao.entity.Cotacao;
import com.gestao.entity.Usuario;
import com.gestao.repository.CotacaoRepository;
import com.gestao.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CotacaoService {

    @Autowired
    private CotacaoRepository cotacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Taxas Padrão
    private static final BigDecimal TAXA_PREMIO_PADRAO = new BigDecimal("0.02"); // 0.02%
    private static final BigDecimal TAXA_CORRETAGEM_PADRAO = new BigDecimal("0.05"); // 5%
    private static final int SCALE = 2;

    @Transactional
    public CotacaoResponse criarCotacao(Long usuarioId, CotacaoRequest cotacaoRequest) {
        log.info("Criando nova cotação para usuário ID: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        validateCotacaoData(cotacaoRequest);

        Cotacao cotacao = new Cotacao();
        cotacao.setUsuario(usuario);
        cotacao.setStatus("ATIVA");
        calculaValores(cotacao, cotacaoRequest);

        Cotacao savedCotacao = cotacaoRepository.save(cotacao);
        log.info("Cotação criada com sucesso: {}", savedCotacao.getId());

        return mapToDTO(savedCotacao);
    }

    @Transactional(readOnly = true)
    public List<CotacaoResponse> listarCotacoesDoUsuario(Long usuarioId) {
        log.info("Listando cotações do usuário: {}", usuarioId);
        return cotacaoRepository.findByUsuarioId(usuarioId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CotacaoResponse obterCotacaoPorId(Long id) {
        log.info("Obtendo cotação: {}", id);
        Cotacao cotacao = cotacaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cotação não encontrada"));
        return mapToDTO(cotacao);
    }

    @Transactional
    public void deletarCotacao(Long id) {
        log.info("Deletando cotação: {}", id);
        Cotacao cotacao = cotacaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cotação não encontrada"));
        cotacao.setStatus("EXCLUIDA");
        cotacaoRepository.save(cotacao);
        log.info("Cotação deletada com sucesso");
    }

    @Transactional
    public CotacaoResponse atualizarCotacao(Long id, CotacaoRequest cotacaoRequest) {
        log.info("Atualizando cotação: {}", id);

        Cotacao cotacao = cotacaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cotação não encontrada"));

        validateCotacaoData(cotacaoRequest);
        calculaValores(cotacao, cotacaoRequest);

        Cotacao savedCotacao = cotacaoRepository.save(cotacao);
        log.info("Cotação atualizada com sucesso");

        return mapToDTO(savedCotacao);
    }

    /**
     * Calcula os valores financeiros do prêmio e os aplica na cotação informada.
     * Usado tanto na criação quanto na atualização de cotações.
     */
    private void calculaValores(Cotacao cotacao, CotacaoRequest request) {
        BigDecimal premioBruto = request.getValorEmprestimo()
                .multiply(TAXA_PREMIO_PADRAO)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal corretagemValor = premioBruto
                .multiply(TAXA_CORRETAGEM_PADRAO)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal premioTotal = premioBruto.add(corretagemValor)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal valorParcelado = premioTotal
                .divide(new BigDecimal(request.getPrazoMeses()), SCALE, RoundingMode.HALF_UP);

        cotacao.setValorEmprestimo(request.getValorEmprestimo());
        cotacao.setPrazoMeses(request.getPrazoMeses());
        cotacao.setTaxaPremio(TAXA_PREMIO_PADRAO);
        cotacao.setTaxaCorretagem(TAXA_CORRETAGEM_PADRAO);
        cotacao.setPremioBruto(premioBruto);
        cotacao.setCorretagemValor(corretagemValor);
        cotacao.setPremioTotal(premioTotal);
        cotacao.setValorAVista(premioTotal); // à vista = total; desconto futuro aplicável aqui
        cotacao.setValorParcelado(valorParcelado);
    }

    private static void validateCotacaoData(CotacaoRequest cotacaoRequest) {
        if (cotacaoRequest.getValorEmprestimo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do empréstimo deve ser maior que zero");
        }
        if (cotacaoRequest.getPrazoMeses() < 12) {
            throw new IllegalArgumentException("Prazo mínimo é 12 meses");
        }
    }

    private CotacaoResponse mapToDTO(Cotacao cotacao) {
        return CotacaoResponse.builder()
            .id(cotacao.getId())
            .valorEmprestimo(cotacao.getValorEmprestimo())
            .prazoMeses(cotacao.getPrazoMeses())
            .taxaPremio(cotacao.getTaxaPremio())
            .taxaCorretagem(cotacao.getTaxaCorretagem())
            .premioBruto(cotacao.getPremioBruto())
            .corretagemValor(cotacao.getCorretagemValor())
            .premioTotal(cotacao.getPremioTotal())
            .valorAVista(cotacao.getValorAVista())
            .valorParcelado(cotacao.getValorParcelado())
            .status(cotacao.getStatus())
            .criadaEm(cotacao.getCriadaEm() != null ? cotacao.getCriadaEm().format(DATE_FORMATTER) : null)
            .build();
    }
}
