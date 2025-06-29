package cl.tenpo.calculation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.tenpo.calculation.dto.ErrorResponseDto;
import cl.tenpo.calculation.dto.PageDto;
import cl.tenpo.calculation.dto.RequestAuditDto;
import cl.tenpo.calculation.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;

/**
 * Controlador para manejar las peticiones relacionadas con la auditoría de requests.
 */
@RestController
@RequestMapping("/audit")
@Tag(name = "API para consultar la auditoría de requests", description = "Permite consultar los requests auditados generados por los usuarios de la API")
public class AuditController {

	@Autowired
	private AuditService auditService;

	/**
	 * Obtiene una lista paginada de requests auditados.
	 * 
	 * @param page Pagina a obtener, por defecto 0.
	 * @param size Tamaño de la pagina a obtener, por defecto 10.
	 * @return
	 */
	@GetMapping
	@Operation(summary = "Obtiene una lista paginada de requests auditados", description = "Dados un número de página y un tamaño de página, devuelve una lista paginada de requests auditados.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
			@ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))})	
	public PageDto<RequestAuditDto> getAudit(
			@RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "page debe ser >= 0") @Parameter(description = "Número de página", required = false) int page,
			@RequestParam(name = "size", defaultValue = "10") @Min(value = 0, message = "size debe ser >= 0") @Parameter(description = "Tamaño de página", required = false) int size) {
		return auditService.getAll(page, size);
	}
}
