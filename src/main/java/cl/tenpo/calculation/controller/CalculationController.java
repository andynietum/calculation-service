package cl.tenpo.calculation.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.tenpo.calculation.dto.ErrorResponseDto;
import cl.tenpo.calculation.service.AuditService;
import cl.tenpo.calculation.service.CalculationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Controlador para manejar los requests de cálculo de porcentajes.
 */
@RestController
@RequestMapping("/calculation")
@Validated
@Tag(name = "API para realizar el cálculo de la operación", description = "Permite calcular la operación indicada: El"
		+ " servicio debe sumar ambos números y aplicar un porcentaje adicional al resultado de esa suma")
public class CalculationController {

	@Autowired
	CalculationService calcService;

	@Autowired
	AuditService auditService;

	/**
	 * Realiza la operación de cálculo de porcentaje.
	 * 
	 * @param num1 Primer operando de la operación.
	 * @param num2 Segundo operando de la operación.
	 * @return El resultado de la operación de cálculo de porcentaje.
	 */
	@GetMapping
	@RateLimiter(name = "percentageCalculation")
	@Operation(summary = "Calcular suma con porcentaje", description = "Suma num1 y num2, y aplica un porcentaje adicional")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Cálculo exitoso"),
			@ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
			@ApiResponse(responseCode = "429", description = "Demasiadas solicitudes (rate limit excedido)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
			@ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})
	public BigDecimal calculate(
			@RequestParam(name = "num1") @NotNull(message = "num1 es obligatorio") @Min(value = 0, message = "num1 debe ser >= 0") @Parameter(description = "Primer número entero de la entrada de la operacion (int)", required = true) int num1,
			@RequestParam(name = "num2") @NotNull(message = "num1 es obligatorio") @Min(value = 0, message = "num1 debe ser >= 0") @Parameter(description = "Segundo número entero de la entrada de la operacion (int)", required = true) int num2) {
		return this.calcService.calculate(num1, num2);
	}
}
