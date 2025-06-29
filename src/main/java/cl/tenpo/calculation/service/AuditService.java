package cl.tenpo.calculation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cl.tenpo.calculation.dto.PageDto;
import cl.tenpo.calculation.dto.RequestAuditDto;
import cl.tenpo.calculation.entity.RequestAudit;
import cl.tenpo.calculation.repository.RequestAuditRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio encargado de auditar/consultar los requests realizados al servicio.
 */
@Service
@Slf4j
public class AuditService {

	@Autowired
	RequestAuditRepository requestAuditRepository;

	/**
	 * Registra un request realizado al servicio.
	 * Este método se ejecuta de forma asíncrona para no bloquear el hilo principal
	 * 
	 * @param requestTime Hora del request 
	 * @param endpoint Endpoint del request
	 * @param incoming Lista de parámetros de entrada del request
	 * @param result Resultado del request
	 * @param success Indica si el request fue exitoso o no
	 */
	@Async
	public void auditRequest(LocalDateTime requestTime, String endpoint, List<?> incoming, Object result,
			boolean success) {
		RequestAudit requestLog = 
				RequestAudit.builder()
					.requestTime(requestTime)
					.endpoint(endpoint)
					.incoming(incoming != null ? incoming.toString() : null)
					.result(result != null ? result.toString() : null)
					.success(success)
					.build();
		this.requestAuditRepository.save(requestLog);// Simulate an exception for testing
		log.info("Audit request processed." + requestLog);
	}

	/**
	 * Obtiene una lista paginada de los requests realizados al servicio.
	 * 
	 * @param pageNumber Número de página a obtener
	 * @param size Tamaño de la página a obtener
	 * @return PageDto<RequestAuditDto> Lista paginada de los requests realizados al servicio
	 */
	public PageDto<RequestAuditDto> getAll(int pageNumber, int size) {
		Page<RequestAudit> page = this.requestAuditRepository.findAll(PageRequest.of(pageNumber, size));
		return new PageDto<>(
				page.getContent().stream()
						.map(log -> new RequestAuditDto(log.getRequestTime(), log.getEndpoint(), log.getIncoming(),
								log.getResult(), log.isSuccess()))
						.toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
	}
}
