package com.gestao.controller;

import com.gestao.dto.RegistroDTO;
import com.gestao.dto.LoginDTO;
import com.gestao.dto.AuthResponseDTO;
import com.gestao.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "APIs de autenticação e registro de usuários")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    @Operation(summary = "Registrar novo usuário", 
               description = "Cria um novo usuário e retorna um token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Usuário ou email já existe")
    })
    public ResponseEntity<AuthResponseDTO> registrar(@Valid @RequestBody RegistroDTO registroDTO) {
        log.info("POST /auth/registro - Registrando usuário: {}", registroDTO.getUsername());
        AuthResponseDTO response = authService.registrar(registroDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", 
               description = "Autentica um usuário e retorna um token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("POST /auth/login - Login do usuário: {}", loginDTO.getUsername());
        AuthResponseDTO response = authService.login(loginDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

