package cl.tenpo.calculation.aspect;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cl.tenpo.calculation.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Aspecto para auditar los requests a los controllers.
 */
@Aspect
@Component
public class AuditAspect {

	private static final String METHOD_OPERATION_SEPARATOR = " ";
	
	@Autowired
	AuditService auditService;

	/**
	 * Intercepta las peticiones a los controllers y registra la información
	 * necesaria para la auditoría.
	 * 
	 * @param joinPoint El punto de unión que representa la llamada al método del
	 * controller.
	 * @return El resultado del método del controller.
	 * @throws Throwable Si ocurre algún error durante la ejecución del método del
	 * 	controller.
	 */
	@Around("execution(* cl.tenpo.calculation.controller..*Controller.*(..))")
	public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
		LocalDateTime requestTime = LocalDateTime.now();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String endpoint = request.getMethod() + METHOD_OPERATION_SEPARATOR + request.getRequestURI();
		List<Object> params = Arrays.asList(joinPoint.getArgs());
		Object result = null;
		boolean success = true;
		Object response = null;
		try {
			result = joinPoint.proceed();
			response = result;
			return result;
		} catch (Throwable ex) {
			success = false;
			response = ex.getMessage();
			throw ex;
		} finally {
			this.auditService.auditRequest(requestTime, endpoint, params, response, success);
		}
	}
}
