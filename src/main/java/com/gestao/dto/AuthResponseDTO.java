package com.gestao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO para resposta de autenticação com token JWT")
public class AuthResponseDTO {

    @Schema(description = "Token JWT para autenticação em requisições futuras", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Tipo de token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Tempo de expiração do token em segundos", example = "86400")
    private Long expiresIn;

    @Schema(description = "ID do usuário autenticado", example = "1")
    private Long usuarioId;

    @Schema(description = "Username do usuário autenticado", example = "joao_silva")
    private String username;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String tokenType, Long expiresIn, Long usuarioId, String username) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.usuarioId = usuarioId;
        this.username = username;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String tokenType = "Bearer";
        private Long expiresIn;
        private Long usuarioId;
        private String username;

        public Builder token(String token) { this.token = token; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(Long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public Builder username(String username) { this.username = username; return this; }

        public AuthResponseDTO build() {
            return new AuthResponseDTO(token, tokenType, expiresIn, usuarioId, username);
        }
    }
}

