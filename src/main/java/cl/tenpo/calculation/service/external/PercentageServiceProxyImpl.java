package cl.tenpo.calculation.service.external;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.tenpo.calculation.service.PercentageService;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Proxy del servicio externo desde donde se obtiene el porcentaje a aplicar 
 * en la operación. Utiliza una cache en Redis para almacenar el valor del porcentaje
 * calculado para el caso de que el servicio externo falle o no esté disponible temporalmente 
 * se pueda obtener el valor desde la cache por un tiempo determinado.
 */
@Service(value = "percentageService")
public class PercentageServiceProxyImpl implements PercentageService {
	
	private static final String PERCENTAGE_UNAVAILABLE_MESSAGE = "Porcentaje no disponible temporalmente";

	private static final String CACHE_KEY = "percentage";
	
	private final StringRedisTemplate redisTemplate;
	 
	private final PercentageService percentageService;	 

	private final String percentageTtl;	
	
	public PercentageServiceProxyImpl(StringRedisTemplate redisTemplate, 
			@Qualifier("externalPercentageService") PercentageService percentageService,
			@Value("${percentage.cache.ttl:PT30M}") String percentageTtl) {
		this.redisTemplate = redisTemplate;
		this.percentageService = percentageService;
		this.percentageTtl = percentageTtl;
	}
	
    /**
     * Obtiene el porcentaje a aplicar en la operación desde el servicio externo.
     * Además, almacena el valor en la cache de Redis por un tiempo de vida definido.
     * Tiene configurado un mecanismo de reintentos para manejar fallos temporales
     * del servicio externo y en ese caso poder obtener el valor desde la cache.
     */
    @Retry(name = "percentageRetry", fallbackMethod = "getFromCacheOrFail")
    @Override
    public BigDecimal getPercentage() {
    	BigDecimal value = percentageService.getPercentage();
    	redisTemplate.opsForValue().set(CACHE_KEY, value.toString(), Duration.parse(percentageTtl));
    	return value;
    }

    /**
     * Método de fallback que se ejecuta en caso de que el servicio externo falle
     * o no esté disponible temporalmente.Si ese es el caso, intenta obtener	
     * el valor del porcentaje desde la cache de Redis.
     * Si no se encuentra el valor en la cache, lanza una excepción
     * 
     * @param ex La excepción que causó la ejecucion del metodo	de fallback
     * @return El valor del porcentaje obtenido de la cache
     * @throws ResponseStatusException Si no se encuentra el valor en la cache
     */
    public BigDecimal getFromCacheOrFail(Exception ex) {
        String cachedValue = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedValue != null) {
            return new BigDecimal(cachedValue);
        } else {
        	throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, PERCENTAGE_UNAVAILABLE_MESSAGE);

        }
    }

}
