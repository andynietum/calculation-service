package cl.tenpo.calculation.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cl.tenpo.calculation.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Test unitarios para la clase {@link AuditAspect}.
 */
@ExtendWith(MockitoExtension.class)
public class AuditAspectTest {

    @InjectMocks
    private AuditAspect auditAspect;

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setup() {
        // Mock del contexto web de Spring
        ServletRequestAttributes attributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    /**
     * Test para verificar que el aspecto de auditoría registra correctamente un request exitoso.
     * 
     * @throws Throwable si ocurre algún error durante la ejecución del join point.
     */
    @Test    
    @DisplayName("Debería auditar correctamente un request exitoso")    
    void testLogRequestSuccess() throws Throwable {
        // Given
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURI()).thenReturn("/calculate");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"5", "10"});
        when(joinPoint.proceed()).thenReturn("resultado-ok");

        // When
        Object result = auditAspect.logRequest(joinPoint);

        // Then
        assertEquals("resultado-ok", result);

        verify(auditService).auditRequest(
            any(LocalDateTime.class),
            eq("GET /calculate"),
            eq(List.of("5", "10")),
            eq("resultado-ok"),
            eq(true)
        );
    }

    /**
     * Test para verificar que el aspecto de auditoría registra correctamente un request fallido
     */
    @Test
    @DisplayName("Debería auditar correctamente un request fallido con excepción lanzada")
    void testLogRequestThrowsException() {
        // Given
        RuntimeException simulatedEx = new RuntimeException("error esperado");
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletRequest.getRequestURI()).thenReturn("/calcular");
        when(joinPoint.getArgs()).thenReturn(new Object[]{1, 2});
        try {
            when(joinPoint.proceed()).thenThrow(simulatedEx);
        } catch (Throwable ignored) {}

        // When
        Exception thrown = assertThrows(RuntimeException.class, () -> {
            auditAspect.logRequest(joinPoint);
        });

        // Then
        assertEquals("error esperado", thrown.getMessage());

        verify(auditService).auditRequest(
            any(LocalDateTime.class),
            eq("POST /calcular"),
            eq(List.of(1, 2)),
            eq("error esperado"),
            eq(false)
        );
    }
}
