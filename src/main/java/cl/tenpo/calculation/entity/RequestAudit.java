package cl.tenpo.calculation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un registro de auditoría de un request realizado al servicio.
 */
@Entity
@Table(name = "request_audit")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestAudit {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDateTime requestTime;
	
	private String endpoint;
	
	@Column(length = 2048)
	private String incoming;
	
	@Column(length = 4096)
	private String result;
	
	private boolean success;
}
