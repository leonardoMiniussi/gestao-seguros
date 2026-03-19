package com.gestao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para resposta de cálculo de seguro prestamista")
public class SeguroResponseDTO {

    @Schema(description = "ID do seguro", example = "1")
    private Long id;

    @Schema(description = "ID do usuário titular do seguro", example = "1")
    private Long usuarioId;

    @Schema(description = "ID da cotação que originou o seguro", example = "1")
    private Long cotacaoId;

    @Schema(description = "Valor do empréstimo", example = "10000.00")
    private BigDecimal valorEmprestimo;

    @Schema(description = "Prazo em meses", example = "36")
    private Integer prazoMeses;

    @Schema(description = "Taxa de prêmio aplicada (0.02%)", example = "0.02")
    private BigDecimal taxaPremio;

    @Schema(description = "Taxa de corretagem aplicada (5%)", example = "0.05")
    private BigDecimal taxaCorretagem;

    @Schema(description = "Prêmio bruto calculado (valorEmprestimo * taxaPremio)", example = "2.00")
    private BigDecimal premioBruto;

    @Schema(description = "Valor da corretagem (premioBruto * taxaCorretagem)", example = "0.10")
    private BigDecimal corretagemValor;

    @Schema(description = "Prêmio total (premioBruto + corretagemValor)", example = "2.10")
    private BigDecimal premioTotal;

    @Schema(description = "Valor a ser pago à vista", example = "2.10")
    private BigDecimal valorAVista;

    @Schema(description = "Valor de cada parcela mensal", example = "0.06")
    private BigDecimal valorParcelado;

    @Schema(description = "Status do seguro", example = "ATIVO")
    private String status;

    @Schema(description = "Data de criação do seguro", example = "2024-03-11T10:30:00")
    private String criadoEm;

    @Schema(description = "Detalhamento dos valores calculados")
    private String detalhamento;
}
