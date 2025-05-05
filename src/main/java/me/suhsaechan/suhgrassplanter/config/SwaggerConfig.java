package me.suhsaechan.suhgrassplanter.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@OpenAPIDefinition(
    info = @Info(
        title = "SUH GRASS PLANTER",
        description = """
            SUH-GRASS-PLANTER 공식 API 문서
            """,
        version = "v0.1.0"
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "로컬 서버"),
        @Server(url = "http://suh-project.synology.me:8094", description = "SUH-PROJECT - 서버")
    }
)
@Configuration
public class SwaggerConfig implements WebMvcConfigurer {

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme apiKey = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .in(SecurityScheme.In.HEADER)
        .name("Authorization")
        .scheme("bearer")
        .bearerFormat("JWT");

    SecurityRequirement securityRequirement = new SecurityRequirement()
        .addList("Bearer Token");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
        .addSecurityItem(securityRequirement)
        .servers(List.of(
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://localhost:8080")
                    .description("로컬 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://suh-project.synology.me:8092")
                    .description("SUH-PROJECT 서버")
            )
        );
  }
}