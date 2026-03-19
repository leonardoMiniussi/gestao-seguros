package com.gestao.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "cotacoes")
@AllArgsConstructor
public class Cotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "Valor do empréstimo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor do empréstimo deve ser maior que 0")
    @Column(name = "valor_emprestimo", nullable = false)
    private BigDecimal valorEmprestimo;

    @NotNull(message = "Prazo é obrigatório")
    @Min(value = 12, message = "Prazo mínimo é 12 meses")
    @Column(name = "prazo_meses", nullable = false)
    private Integer prazoMeses;

    @Schema(description = "Taxa de prêmio aplicada", example = "0.0002")
    private BigDecimal taxaPremio;

    @Column(name = "taxa_corretagem")
    private BigDecimal taxaCorretagem;

    @Column(name = "premio_bruto")
    private BigDecimal premioBruto;

    @Column(name = "corretagem_valor")
    private BigDecimal corretagemValor;

    @Column(name = "premium_total")
    private BigDecimal premioTotal;

    @Column(name = "valor_avista")
    private BigDecimal valorAVista;

    @Column(name = "valor_parcelado")
    private BigDecimal valorParcelado;

    @Column(name = "status")
    private String status = "ATIVA";

    @Column(name = "criada_em")
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;

    @PrePersist
    protected void onCreate() {
        criadaEm = LocalDateTime.now();
        atualizadaEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadaEm = LocalDateTime.now();
    }

    public Cotacao() {}
}

