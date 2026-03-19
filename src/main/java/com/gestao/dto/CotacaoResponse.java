package com.gestao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(description = "DTO para response de criação de cotações")
public class CotacaoResponse {

    @Schema(description = "ID da cotação", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Valor do empréstimo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do empréstimo deve ser maior que 0")
    @Schema(description = "Valor do empréstimo em reais", example = "10000.00")
    private BigDecimal valorEmprestimo;

    @NotNull(message = "Prazo é obrigatório")
    @Min(value = 12, message = "Prazo mínimo é 12 meses")
    @Schema(description = "Prazo do empréstimo em meses (mínimo 12)", example = "36")
    private Integer prazoMeses;

    @Schema(description = "Taxa de prêmio aplicada", example = "0.0002")
    private BigDecimal taxaPremio;

    @Schema(description = "Taxa de corretagem aplicada", example = "0.05")
    private BigDecimal taxaCorretagem;

    @Schema(description = "Prêmio bruto calculado (valor_emprestimo * taxa_premio)", example = "2.00")
    private BigDecimal premioBruto;

    @Schema(description = "Valor da corretagem (premio_bruto * taxa_corretagem)", example = "0.10")
    private BigDecimal corretagemValor;

    @Schema(description = "Prêmio total calculado", example = "630.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal premioTotal;

    @Schema(description = "Valor a ser pago à vista", example = "2.10")
    private BigDecimal valorAVista;

    @Schema(description = "Valor parcelado", example = "17.50", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal valorParcelado;

    @Schema(description = "Status da cotação", example = "ATIVA", accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    @Schema(description = "Data de criação", example = "2024-03-11T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private String criadaEm;
}
