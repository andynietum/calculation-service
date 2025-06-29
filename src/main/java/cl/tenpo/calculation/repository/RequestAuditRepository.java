package cl.tenpo.calculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tenpo.calculation.entity.RequestAudit;

/**
 * Repositorio para manejar las auditorías de requests.
 */
public interface RequestAuditRepository extends JpaRepository<RequestAudit, Long> {}
