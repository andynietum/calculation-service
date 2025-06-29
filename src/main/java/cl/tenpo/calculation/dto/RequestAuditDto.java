package cl.tenpo.calculation.dto;

import java.time.LocalDateTime;

/**
 * DTO para auditoría de requests.
 */
public record RequestAuditDto(
		LocalDateTime requestTime,
		String endpoint,
	    String incoming,
	    String result,	    	
	    boolean success
	) {}
