package cl.tenpo.calculation.service;

import java.math.BigDecimal;

/**
 * Interface de Servicio para obtener el porcentaje a aplicar en la operación.
 */
public interface PercentageService {
	
	/**
	 * Obtiene el porcentaje a aplicar en la operación.
	 * 
	 * @return
	 */
	BigDecimal getPercentage();

}
