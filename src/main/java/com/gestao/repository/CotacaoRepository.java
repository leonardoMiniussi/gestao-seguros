package com.gestao.repository;

import com.gestao.entity.Cotacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CotacaoRepository extends JpaRepository<Cotacao, Long> {
    List<Cotacao> findByUsuarioId(Long usuarioId);
}

