package cl.tenpo.calculation.service.external;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import cl.tenpo.calculation.service.PercentageService;

/**
 * Mock del servicio externo que retorna un porcentaje fijo.
 */
@Service(value = "externalPercentageService")
public class PercentageServiceExternalMockedImpl implements PercentageService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BigDecimal getPercentage() {
		return BigDecimal.TEN;
	}
}
