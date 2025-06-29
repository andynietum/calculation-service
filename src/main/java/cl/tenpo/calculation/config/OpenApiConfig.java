package cl.tenpo.calculation.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Configuración de OpenAPI para la documentación de la API.
 */
@Configuration
@EnableConfigurationProperties(ApiDocsProperties.class)
public class OpenApiConfig {
	
    private final ApiDocsProperties props;

    public OpenApiConfig(ApiDocsProperties props) {
        this.props = props;
    }

    /**
     * Bean para configurar OpenAPI con la información de la API.
     * 
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(props.title())
                .version(props.version())
                .description(props.description())
                .contact(new Contact()
                    .name(props.contactName())
                    .email(props.contactEmail()))
                .license(new License()
                    .name(props.licenseName())
                    .url(props.licenseUrl())));
    }

}
