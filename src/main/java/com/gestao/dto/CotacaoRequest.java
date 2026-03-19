package com.gestao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@Schema(description = "DTO para criação e atualização de cotações")
public class CotacaoRequest {

    @NotNull(message = "Valor do empréstimo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do empréstimo deve ser maior que 0")
    @Schema(description = "Valor do empréstimo em reais", example = "10000.00")
    private BigDecimal valorEmprestimo;

    @NotNull(message = "Prazo é obrigatório")
    @Min(value = 12, message = "Prazo mínimo é 12 meses")
    @Schema(description = "Prazo do empréstimo em meses (mínimo 12)", example = "36")
    private Integer prazoMeses;

    public CotacaoRequest() {}

    public CotacaoRequest(BigDecimal valorEmprestimo, Integer prazoMeses) {
        this.valorEmprestimo = valorEmprestimo;
        this.prazoMeses = prazoMeses;
    }
}

