#  Gestão de Seguros API

## Tecnologias Utilizadas

- **Spring Boot 3.2.0** - Framework Web
- **Spring Security** - Autenticação e Autorização
- **JWT (JSON Web Token)** - JJWT 0.12.3
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória (desenvolvimento)
- **OpenAPI 3.0 / Springdoc** - Documentação automática de API
- **Lombok** - Redução de boilerplate code
- **Jakarta Validation** - Validação de dados

## Pré-requisitos
- Java 17+ instalado
- Maven 3.6+ instalado
- Git

### Verificar Instalações
```bash
java -version
mvn -version
```

---
## Configuração

### Download
```powershell
git clon
cd <path>\gestao-seguros
```

### Instale as dependências:
```powershell
mvn clean install
```

### Executar a Aplicação

```bash
mvn spring-boot:run
```
---

## Acessar a API

### URLs Principais

| Recurso | URL |
|---------|-----|
| **Home** | http://localhost:8080 |
| **API** | http://localhost:8080/api |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api/v3/api-docs |
| **H2 Console** | http://localhost:8080/api/h2-console |

---

## Testar com Postman

1. **Importar Collection:**
   - Abrir Postman
   - Clicar em "Import"
   - Selecionar arquivo: `Gestao_Seguros_API.postman_collection.json`

2. **Configurar Variáveis:**
   - Na aba "Variables" da collection
   - Definir `base_url`: `http://localhost:8080/api`
   - Após autenticação, definir `jwt_token` com o token retornado
---

## Swagger
Abrir navegador: `http://localhost:8080/api/swagger-ui.html`

---

## H2 Console (Visualizar BD)
1. Acessar: `http://localhost:8080/api/h2-console`
2. Conectar com:
   - JDBC URL: `jdbc:h2:mem:gestao_seguros`
   - User Name: `sa`
   - Password: (deixar vazio)
3. Clicar "Connect"
4. Executar SQL:
```sql
SELECT * FROM usuarios;
SELECT * FROM cotacoes;
SELECT * FROM seguros;
```


## Dicas Úteis

### Alterar Taxa de Prêmio/Corretagem
Editar em `CotacaoService.java`:
```java
private static final BigDecimal TAXA_PREMIO_PADRAO = new BigDecimal("0.02"); // 2%
private static final BigDecimal TAXA_CORRETAGEM_PADRAO = new BigDecimal("0.05"); // 5%
```

### Usar com Docker
Build e run:
```bash
docker-compose up
```

