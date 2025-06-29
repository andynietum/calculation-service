package cl.tenpo.calculation.dto;

import java.time.ZonedDateTime;

/**
 * Dto para representar una respuesta de error.
 */
public record ErrorResponseDto(
	int status,
	String message,
	ZonedDateTime timestamp
) {}
