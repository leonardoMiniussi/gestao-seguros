package com.gestao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotacao_id", nullable = false, unique = true)
    private Cotacao cotacao;

    @NotNull(message = "Valor do empréstimo é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que 0")
    @Column(name = "valor_emprestimo", nullable = false)
    private BigDecimal valorEmprestimo;

    @NotNull(message = "Prazo é obrigatório")
    @Min(value = 12, message = "Prazo mínimo é 12 meses")
    @Column(name = "prazo_meses", nullable = false)
    private Integer prazoMeses;

    @Column(name = "taxa_premio", nullable = false)
    private BigDecimal taxaPremio;

    @Column(name = "taxa_corretagem", nullable = false)
    private BigDecimal taxaCorretagem;

    @Column(name = "premio_bruto", nullable = false)
    private BigDecimal premioBruto;

    @Column(name = "corretagem_valor", nullable = false)
    private BigDecimal corretagemValor;

    @Column(name = "premio_total", nullable = false)
    private BigDecimal premioTotal;

    @Column(name = "valor_a_vista", nullable = false)
    private BigDecimal valorAVista;

    @Column(name = "valor_parcelado", nullable = false)
    private BigDecimal valorParcelado;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ATIVO";

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
