package com.gestao.controller;

import com.gestao.dto.SeguroResponseDTO;
import com.gestao.security.JwtTokenProvider;
import com.gestao.service.SeguroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/seguros")
@Tag(name = "Seguros", description = "APIs para gerenciamento de seguros prestamistas")
@SecurityRequirement(name = "bearer-jwt")
public class SeguroController {

    @Autowired
    private SeguroService seguroService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/aprovar/{cotacaoId}")
    @Operation(
            summary = "Aprovar cotação e emitir seguro",
            description = """
                    Aprova uma cotação ATIVA do usuário autenticado e emite o seguro prestamista correspondente.
                    
                    Regras:
                    - A cotação deve pertencer ao usuário autenticado.
                    - A cotação deve estar com status **ATIVA**.
                    - Após a aprovação, o status da cotação é alterado para **APROVADA** e um Seguro com status **ATIVO** é criado.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seguro emitido com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeguroResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Cotação não está ativa para aprovação"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Cotação não pertence ao usuário autenticado"),
            @ApiResponse(responseCode = "404", description = "Cotação não encontrada")
    })
    public ResponseEntity<SeguroResponseDTO> aprovarCotacao(@PathVariable Long cotacaoId) {
        Long usuarioId = extrairUsuarioIdDoToken();
        log.info("POST /seguros/aprovar/{} - Usuário ID: {}", cotacaoId, usuarioId);
        SeguroResponseDTO seguro = seguroService.aprovarCotacao(cotacaoId, usuarioId);
        return new ResponseEntity<>(seguro, HttpStatus.CREATED);
    }

    @GetMapping("/meus-seguros")
    @Operation(
            summary = "Listar meus seguros",
            description = "Retorna todos os seguros emitidos do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de seguros do usuário",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SeguroResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<SeguroResponseDTO>> listarMeusSeguros() {
        Long usuarioId = extrairUsuarioIdDoToken();
        log.info("GET /seguros/meus-seguros - Usuário ID: {}", usuarioId);
        List<SeguroResponseDTO> seguros = seguroService.listarSegurosDoUsuario(usuarioId);
        return new ResponseEntity<>(seguros, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obter detalhes de um seguro",
            description = "Retorna os detalhes de um seguro específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seguro encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeguroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Seguro não encontrado")
    })
    public ResponseEntity<SeguroResponseDTO> obterSeguro(@PathVariable Long id) {
        log.info("GET /seguros/{} - Obtendo detalhes do seguro", id);
        SeguroResponseDTO seguro = seguroService.obterSeguroPorId(id);
        return new ResponseEntity<>(seguro, HttpStatus.OK);
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
