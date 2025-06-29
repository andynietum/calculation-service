package cl.tenpo.calculation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import cl.tenpo.calculation.dto.ErrorResponseDto;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;

/**
 * Tests unitarios para {@link GlobalExceptionHandler}
 */
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * Verifica que el manejador de excepciones retorne un error 429 (Too Many Requests)	
     * cuando se excede el límite de peticiones del rate limiter.
     */
    @Test
    @DisplayName("Debería retornar 429 Too Many Requests al exceder el rate limit")
    void shouldReturn429WhenRateLimitExceeded() {
        RequestNotPermitted ex = mock(RequestNotPermitted.class);
        when(ex.getMessage()).thenReturn("Límite de requests excedido");

        ResponseEntity<ErrorResponseDto> response = handler.handleRateLimitExceeded(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Límite de requests excedido", response.getBody().message());
    }

    /**
     * Verifica que el manejador de excepciones retorne un error 400 (Bad Request)
     * cuando se lanza una {@link IllegalArgumentException}
     */
    @Test
    @DisplayName("Debería retornar 400 Bad Request para IllegalArgumentException")
    void shouldReturn400ForIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Parámetro inválido");

        ResponseEntity<ErrorResponseDto> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Parámetro inválido", response.getBody().message());
    }

    /**
     * Verifica que el manejador de excepciones retorne un error 400 (Bad Request)
     * cuando se lanza una {@link ConstraintViolationException}
     */
    @Test
    @DisplayName("Debería retornar 400 Bad Request para ConstraintViolationException")
    void shouldReturn400ForConstraintViolation() {
        ConstraintViolationException ex = new ConstraintViolationException("Violación de restricción", null);

        ResponseEntity<ErrorResponseDto> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Violación de restricción", response.getBody().message());
    }

    /**
     * Verifica que el manejador de excepciones retorne un error 400 (Bad Request)
     * cuando se lanza una {@link MethodArgumentTypeMismatchException}
     */
    @Test
    @DisplayName("Debería retornar 400 Bad Request para errores de tipo de parámetro o faltantes")
    void shouldReturn400ForMissingParamOrTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(null, null, "param", null, new RuntimeException("fail"));

        ResponseEntity<ErrorResponseDto> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Verifica que el manejador de excepciones retorne un error 500 (Internal Server Error)
     * cuando se lanza una excepción genérica.
     */
    @Test
    @DisplayName("Debería retornar 500 Internal Server Error para excepciones genéricas")
    void shouldReturn500ForGenericException() {
        Exception ex = new RuntimeException("Error inesperado");

        ResponseEntity<ErrorResponseDto> response = handler.handleInternalError(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno del servidor", response.getBody().message());
    }

    /**
     * Verifica que el manejador de excepciones retorne el estado correcto
     */
    @Test
    @DisplayName("Debería retornar el estado correcto para ResponseStatusException")
    void shouldReturnStatusForResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado");

        ResponseEntity<ErrorResponseDto> response = handler.handleStatusError(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso no encontrado", response.getBody().message());
    }

    /**
     * Verifica que el manejador de excepciones establezca un timestamp válido
     */
    @Test
    @DisplayName("La respuesta debería contener un timestamp válido")
    void timestampShouldBeSetInResponse() {
        Exception ex = new RuntimeException("Con timestamp");
        ResponseEntity<ErrorResponseDto> response = handler.handleInternalError(ex);

        assertNotNull(response.getBody().timestamp());
        assertTrue(response.getBody().timestamp().isBefore(ZonedDateTime.now().plusSeconds(1)));
    }
}
