package com.gestao;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GestaoSegurosApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoSegurosApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Gestão de Seguros")
                .version("1.0.0")
                .description("API REST para gestão de seguros com autenticação JWT, cálculo de prêmios e gestão de cotações")
                .contact(new Contact()
                    .name("Gestão de Seguros")
                    .email("suporte@gestaoseguros.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

