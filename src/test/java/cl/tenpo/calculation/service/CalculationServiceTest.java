package cl.tenpo.calculation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitarios para la clase {@link CalculationService}.
 */
public class CalculationServiceTest {
	
	private PercentageService percentageService = mock(PercentageService.class);
	
	private CalculationService calculationService = new CalculationService(this.percentageService);

	
    /**
     * Verifica que el método calculate haga la operación correctamente
     */
    @Test
    @DisplayName("Debería calcular correctamente la suma con porcentaje")
    void testCalculateWithPercentage() {
        // Arrange
        when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(10)); // 10%

        // Act
        BigDecimal result = calculationService.calculate(5, 5); // (5+5) + 10% = 11.0

        // Assert
        assertEquals(BigDecimal.valueOf(11), result);
        verify(percentageService, times(1)).getPercentage();
    }

    /**
     * Verifica que el método calculate maneje correctamente la suma sin porcentaje
     */
    @Test
    @DisplayName("Debería manejar porcentaje cero sin modificar la suma")
    void testCalculateWithZeroPercentage() {
        when(percentageService.getPercentage()).thenReturn(BigDecimal.ZERO);

        BigDecimal result = calculationService.calculate(7, 3); // 7+3 = 10

        assertEquals(BigDecimal.valueOf(10), result);
    }

    /**
     * Verifica que el método calculate maneje correctamente la suma con porcentaje no entero
     */
    @Test
    @DisplayName("Debería calcular correctamente con porcentaje decimal")
    void testCalculateWithDecimalPercentage() {
        when(percentageService.getPercentage()).thenReturn(new BigDecimal("12.5"));

        BigDecimal result = calculationService.calculate(4, 4); // (4+4)=8, 12.5% de 8 = 9.0

        assertEquals(new BigDecimal("9"), result.stripTrailingZeros());
    }

}
