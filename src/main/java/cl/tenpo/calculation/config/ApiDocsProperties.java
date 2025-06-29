package cl.tenpo.calculation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Registro de propiedades para la configuración de la documentación de la API.
 */
@ConfigurationProperties(prefix = "api.docs")
public record ApiDocsProperties(
    String title,
    String description,
    String version,
    String contactName,
    String contactEmail,
    String licenseName,
    String licenseUrl
) {}
