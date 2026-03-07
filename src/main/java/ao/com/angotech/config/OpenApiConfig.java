package ao.com.angotech.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ntupayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NTUPAY API")
                        .description("Documentação da API NTUPAY para gestão de usuários e autenticação")
                        .version("v1")
                        .contact(new Contact().name("NTUPAY Team").email("tech@ntupay.local"))
                        .license(new License().name("Proprietary"))
                );
    }
}
