package com.gestao.controller;

import com.gestao.dto.CotacaoRequest;
import com.gestao.dto.CotacaoResponse;
import com.gestao.security.JwtTokenProvider;
import com.gestao.service.CotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.List;

@RestController
@RequestMapping("/api/cotacoes")
@Tag(name = "Cotações", description = "APIs para gerenciar cotações de seguros")
@SecurityRequirement(name = "bearer-jwt")
public class CotacaoController {

    private static final Logger log = LoggerFactory.getLogger(CotacaoController.class);

    @Autowired
    private CotacaoService cotacaoService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Listar cotações do usuário",
               description = "Retorna todas as cotações do usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de cotações",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CotacaoRequest.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<CotacaoResponse>> listarCotacoes() {
        Long usuarioId = extrairUsuarioIdDoToken();
        log.info("GET /cotacoes - Listando cotações do usuário: {}", usuarioId);
        List<CotacaoResponse> cotacoes = cotacaoService.listarCotacoesDoUsuario(usuarioId);
        return new ResponseEntity<>(cotacoes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter cotação por ID",
               description = "Retorna os detalhes de uma cotação específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cotação encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CotacaoRequest.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cotação não encontrada")
    })
    public ResponseEntity<CotacaoResponse> obterCotacao(@PathVariable Long id) {
        log.info("GET /cotacoes/{} - Obtendo cotação", id);
        CotacaoResponse cotacao = cotacaoService.obterCotacaoPorId(id);
        return new ResponseEntity<>(cotacao, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Criar nova cotação",
               description = "Cria uma nova cotação para o usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cotação criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CotacaoRequest.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<CotacaoResponse> criarCotacao(@Valid @RequestBody CotacaoRequest cotacaoRequest) {
        Long usuarioId = extrairUsuarioIdDoToken();
        log.info("POST /cotacoes - Criando cotação para usuário: {}", usuarioId);
        CotacaoResponse novaContatacao = cotacaoService.criarCotacao(usuarioId, cotacaoRequest);
        return new ResponseEntity<>(novaContatacao, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cotação",
               description = "Atualiza uma cotação existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cotação atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CotacaoRequest.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cotação não encontrada")
    })
    public ResponseEntity<CotacaoResponse> atualizarCotacao(@PathVariable Long id,
                                                           @Valid @RequestBody CotacaoRequest cotacaoRequest) {
        log.info("PUT /cotacoes/{} - Atualizando cotação", id);
        CotacaoResponse cotacaoAtualizada = cotacaoService.atualizarCotacao(id, cotacaoRequest);
        return new ResponseEntity<>(cotacaoAtualizada, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cotação",
               description = "Deleta uma cotação (marca como excluída)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cotação deletada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Cotação não encontrada")
    })
    public ResponseEntity<Void> deletarCotacao(@PathVariable Long id) {
        log.info("DELETE /cotacoes/{} - Deletando cotação", id);
        cotacaoService.deletarCotacao(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Long extrairUsuarioIdDoToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authHeader = attributes.getRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    Long usuarioId = jwtTokenProvider.getUserIdFromToken(token);
                    if (usuarioId != null && usuarioId > 0) {
                        return usuarioId;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao extrair usuário ID do token: {}", e.getMessage());
        }
        
        throw new RuntimeException("Não foi possível autenticar o usuário");
    }
}

