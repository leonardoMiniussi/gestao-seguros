package com.gestao.service;

import com.gestao.dto.SeguroResponseDTO;
import com.gestao.entity.Cotacao;
import com.gestao.entity.Seguro;
import com.gestao.repository.CotacaoRepository;
import com.gestao.repository.SeguroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeguroService {

    @Autowired
    private SeguroRepository seguroRepository;

    @Autowired
    private CotacaoRepository cotacaoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Aprova uma cotação ativa e cria um Seguro com base nos dados dela.
     * A cotação tem seu status alterado para "APROVADA".
     */
    @Transactional
    public SeguroResponseDTO aprovarCotacao(Long cotacaoId, Long usuarioId) {
        log.info("Aprovando cotação ID: {} para usuário ID: {}", cotacaoId, usuarioId);

        Cotacao cotacao = cotacaoRepository.findById(cotacaoId)
                .orElseThrow(() -> new RuntimeException("Cotação não encontrada com ID: " + cotacaoId));

        if (!cotacao.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Acesso negado: cotação não pertence ao usuário autenticado");
        }

        if (!"ATIVA".equals(cotacao.getStatus())) {
            throw new IllegalStateException("Cotação não pode ser aprovada. Status atual: " + cotacao.getStatus());
        }

        Seguro seguro = Seguro.builder()
                .usuario(cotacao.getUsuario())
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

        Seguro savedSeguro = seguroRepository.save(seguro);

        cotacao.setStatus("APROVADA");
        cotacaoRepository.save(cotacao);

        log.info("Cotação ID: {} aprovada. Seguro criado com ID: {}", cotacaoId, savedSeguro.getId());

        return mapToResponseDTO(savedSeguro);
    }

    @Transactional(readOnly = true)
    public List<SeguroResponseDTO> listarSegurosDoUsuario(Long usuarioId) {
        log.info("Listando seguros do usuário ID: {}", usuarioId);
        return seguroRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SeguroResponseDTO obterSeguroPorId(Long id) {
        log.info("Obtendo seguro ID: {}", id);
        Seguro seguro = seguroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seguro não encontrado com ID: " + id));
        return mapToResponseDTO(seguro);
    }

    private SeguroResponseDTO mapToResponseDTO(Seguro seguro) {
        return SeguroResponseDTO.builder()
                .id(seguro.getId())
                .usuarioId(seguro.getUsuario() != null ? seguro.getUsuario().getId() : null)
                .cotacaoId(seguro.getCotacao() != null ? seguro.getCotacao().getId() : null)
                .valorEmprestimo(seguro.getValorEmprestimo())
                .prazoMeses(seguro.getPrazoMeses())
                .taxaPremio(seguro.getTaxaPremio())
                .taxaCorretagem(seguro.getTaxaCorretagem())
                .premioBruto(seguro.getPremioBruto())
                .corretagemValor(seguro.getCorretagemValor())
                .premioTotal(seguro.getPremioTotal())
                .valorAVista(seguro.getValorAVista())
                .valorParcelado(seguro.getValorParcelado())
                .status(seguro.getStatus())
                .criadoEm(seguro.getCriadoEm() != null ? seguro.getCriadoEm().format(DATE_FORMATTER) : null)
                .detalhamento(String.format(
                        "Prêmio Bruto: R$ %s | Corretagem: R$ %s | Total: R$ %s | Parcela: R$ %s/mês",
                        seguro.getPremioBruto(),
                        seguro.getCorretagemValor(),
                        seguro.getPremioTotal(),
                        seguro.getValorParcelado()
                ))
                .build();
    }
}
