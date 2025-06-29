package cl.tenpo.calculation.service.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cl.tenpo.calculation.service.PercentageService;

/**
 * Test unitarios para {@link PercentageServiceProxyImpl}.
 */
public class CalculationServiceProxyImplTest {
	
	private PercentageService percentageService = mock(PercentageService.class);
	
	private StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
	
	private PercentageServiceProxyImpl proxyService= new PercentageServiceProxyImpl(redisTemplate, percentageService, "PT30M");
	
    @SuppressWarnings("unchecked")
	@BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
    }

	/**
	 * Verifica que el método getPercentage ejecuta correctamente y guarda el valor en cache.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Debe obtener el porcentaje del servicio remoto y guardarlo en cache")
	void testGetPercentage_CacheSuccess() {
		// Arrange
		BigDecimal remoteValue = new BigDecimal("10");
		when(percentageService.getPercentage()).thenReturn(remoteValue);
		ValueOperations<String, String> valueOps = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);

		// Act
		BigDecimal result = proxyService.getPercentage();

		// Assert
		assertEquals(remoteValue, result);
		verify(valueOps, times(1)).set("percentage", "10", Duration.ofMinutes(30));
		verify(percentageService, times(1)).getPercentage();
	}	

    /**
     * Verifica que el método de fallback obtiene el valor desde cache cuando el servicio remoto falla.
     */
    @Test
    @DisplayName("Debe retornar valor desde cache si el servicio remoto falla")
    void testGetPercentageFallback_UsesCache() {
        // Arrange
        when(redisTemplate.opsForValue().get("percentage")).thenReturn("7.75");

        // Act
        BigDecimal result = proxyService.getFromCacheOrFail(new RuntimeException("Servicio caído"));

        // Assert
        assertEquals(new BigDecimal("7.75"), result);
    }

    /**
     * Verifica que el método de fallback lanza la excepción correcta si no hay valor en cache y el servicio falla.
     */
    @Test
    @DisplayName("Debe lanzar excepción si no hay valor en cache y falla el servicio")
    void testGetPercentageFallback_NoCacheThrowsException() {
        // Arrange
        when(redisTemplate.opsForValue().get("percentage")).thenReturn(null);

        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                proxyService.getFromCacheOrFail(new RuntimeException("Servicio externo falló"))
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }
}


