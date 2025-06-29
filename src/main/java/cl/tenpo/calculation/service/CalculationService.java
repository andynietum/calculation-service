package cl.tenpo.calculation.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de realizar el cálculo de la operación indicada: El
 * servicio debe sumar ambos números y aplicar un porcentaje adicional obtenido
 * de un servicio externo (por ejemplo, si recibe num1=5 y num2=5 y el servicio
 * externo retorna un 10%, el resultado será (5 + 5) + 10% = 11).
 */
@Service
public class CalculationService {

	private final PercentageService percentageService;

	public CalculationService(@Qualifier("percentageService") PercentageService percentageService) {
		this.percentageService = percentageService;
	}

	/**
	 * Realiza el cálculo de la operación indicada: Suma ambos números y
	 * aplica un porcentaje adicional obtenido de un servicio externo.
	 * 
	 * @param num1 Primer número de entrada de la operación.
	 * @param num2 Segundo número de entrada de la operación.
	 * @return El resultado del cálculo de la operación
	 */
	public BigDecimal calculate(int num1, int num2) {
		BigDecimal sum = BigDecimal.valueOf(num1 + num2);
		BigDecimal percentage = percentageService.getPercentage();
		return sum.add(sum.multiply(percentage).divide(BigDecimal.valueOf(100)));
	}
}
