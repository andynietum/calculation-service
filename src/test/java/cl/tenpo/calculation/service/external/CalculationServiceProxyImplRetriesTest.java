package cl.tenpo.calculation.service.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import cl.tenpo.calculation.service.PercentageService;

/**
 * Tests de Integración para {@link PercentageServiceProxyImpl} vinculados a reintentos y fallback.
 */
@SpringBootTest
@ActiveProfiles("test")
public class CalculationServiceProxyImplRetriesTest {
	
	@Autowired
	private PercentageServiceProxyImpl percentageServiceProxy;
	
	@MockitoBean(name = "externalPercentageService")
	private PercentageService failingRemoteService;
	
	@MockitoBean
	private StringRedisTemplate redisTemplate;
	
	@MockitoBean
	private ValueOperations<String, String> valueOperations;
	
    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Verifica que el método getPercentage reintenta 3 veces y luego llama al fallback
     */
    @Test
    @DisplayName("Debe reintentar y luego llamar al fallback cuando el servicio remoto falla siempre")
    void testRetryAndFallback() {
        // Arrange: simular que el servicio remoto siempre lanza excepción
        when(failingRemoteService.getPercentage()).thenThrow(new RuntimeException("Servicio caído"));

        // Y que Redis tiene un valor en cache
        when(valueOperations.get("percentage")).thenReturn("15.00");

        // Act
        BigDecimal result = percentageServiceProxy.getPercentage();

        // Assert
        assertEquals(new BigDecimal("15.00"), result);
        verify(failingRemoteService, times(3)).getPercentage(); // 3 intentos: 1 + 2 retries
        verify(valueOperations).get("percentage");
    }

    /**
     * Verifica que el método getPercentage lanza excepción si el servicio falla y no hay cache
     */
    @Test
    @DisplayName("Debe lanzar excepción si el servicio falla y no hay cache")
    void testRetryAndFallbackFail() {
        when(failingRemoteService.getPercentage()).thenThrow(new RuntimeException("Servicio remoto KO"));
        when(valueOperations.get("percentage")).thenReturn(null); // no hay cache

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> percentageServiceProxy.getPercentage());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        verify(failingRemoteService, times(3)).getPercentage();
        verify(valueOperations).get("percentage");
    }

}
