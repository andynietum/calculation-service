package cl.tenpo.calculation.controller;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import cl.tenpo.calculation.dto.ErrorResponseDto;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;

/**
 * Controlador global de excepciones que maneja errores comunes
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de límite de requests excedido.
     * 
     * @param ex La excepción que indica que se ha excedido el límite de requests
     * @return ResponseEntity con un mensaje de error y el estado HTTP 429 (Too Many Requests)
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimitExceeded(RequestNotPermitted ex) {
        return buildResponse("Límite de requests excedido", HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Maneja excepciones vinculadas a requests incorrectos, como argumentos inválidos,
     * violaciones de restricciones, o parámetros de request faltantes.
     * 
     * @param ex La excepción que indica un error en el request
     * @return ResponseEntity con un mensaje de error y el estado HTTP 400 (Bad Request)
     */
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class, 
    	MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones internas del servidor.
     * 
     * @param ex La excepción que indica un error interno del servidor
     * @return ResponseEntity con un mensaje de error y el estado HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleInternalError(Exception ex) {
        return buildResponse("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Maneja excepciones de estado HTTP específicas lanzadas por la aplicación.
     * 	
     * @param ex La excepción que indica un error de estado HTTP
     * @return ResponseEntity con un mensaje de error y el estado HTTP correspondiente
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleStatusError(ResponseStatusException ex) {
        return buildResponse(ex.getReason(), HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    /**
     * Construye una respuesta de error con un mensaje y un estado HTTP específico.
     * 
     * @param message El mensaje de error a incluir en la respuesta
     * @param status El estado HTTP a utilizar en la respuesta
     * @return ResponseEntity con el ErrorResponse y el estado HTTP especificado
     */
    private ResponseEntity<ErrorResponseDto> buildResponse(String message, HttpStatus status) {
        ErrorResponseDto error = new ErrorResponseDto(status.value(), message, ZonedDateTime.now());
        return new ResponseEntity<>(error, status);
    }
}
