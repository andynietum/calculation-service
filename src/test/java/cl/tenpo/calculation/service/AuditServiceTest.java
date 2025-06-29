package cl.tenpo.calculation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import cl.tenpo.calculation.dto.PageDto;
import cl.tenpo.calculation.dto.RequestAuditDto;
import cl.tenpo.calculation.entity.RequestAudit;
import cl.tenpo.calculation.repository.RequestAuditRepository;

/**
 * Test unitarios para la clase {@link AuditService}.
 */
@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private RequestAuditRepository requestAuditRepository;

    @InjectMocks
    private AuditService auditService;

    @Captor
    ArgumentCaptor<RequestAudit> auditCaptor;

    /**
     * Verifica que el método auditRequest guarda correctamente un registro de auditoría
     */
    @Test
    @DisplayName("Debería guardar correctamente un registro de auditoría con parámetros válidos")
    void testAuditRequest_shouldSaveAuditEntry() {
        // Given
        LocalDateTime time = LocalDateTime.now();
        String endpoint = "GET /calculate";
        List<String> params = List.of("5", "10");
        String result = "15";
        boolean success = true;

        // When
        auditService.auditRequest(time, endpoint, params, result, success);

        // Then
        verify(requestAuditRepository).save(auditCaptor.capture());
        RequestAudit saved = auditCaptor.getValue();

        assertEquals(time, saved.getRequestTime());
        assertEquals(endpoint, saved.getEndpoint());
        assertEquals(params.toString(), saved.getIncoming());
        assertEquals(result, saved.getResult());
        assertTrue(saved.isSuccess());
    }

    /**
     * Verifica que el método auditRequest maneja correctamente un error en el resultado
     * 
     */
    @Test
    @DisplayName("Debería manejar correctamente valores nulos en los parámetros de entrada y resultado")
    void testAuditRequest_withNullParamsAndResult_shouldHandleGracefully() {
        // When
        auditService.auditRequest(LocalDateTime.now(), "/endpoint", null, null, false);

        // Then
        verify(requestAuditRepository).save(auditCaptor.capture());
        RequestAudit saved = auditCaptor.getValue();

        assertNull(saved.getIncoming());
        assertNull(saved.getResult());
        assertFalse(saved.isSuccess());
    }

    /**
     * Verifica que el método getAll retorna una página de auditorías correctamente mapeada a DTO
     */
    @Test
    @DisplayName("Debería retornar una página de auditorías correctamente mapeada a DTO")
    void testGetAll_shouldReturnPagedDto() {
        // Given
        RequestAudit audit = RequestAudit.builder()
                .requestTime(LocalDateTime.now())
                .endpoint("GET /test")
                .incoming("[]")
                .result("ok")
                .success(true)
                .build();

        Page<RequestAudit> mockPage = new PageImpl<>(
                List.of(audit),
                PageRequest.of(0, 10),
                1
        );

        when(requestAuditRepository.findAll(PageRequest.of(0, 10))).thenReturn(mockPage);

        // When
        PageDto<RequestAuditDto> result = auditService.getAll(0, 10);

        // Then
        assertEquals(1, result.content().size());
        assertEquals("GET /test", result.content().get(0).endpoint());
        assertEquals("ok", result.content().get(0).result());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
    }
}

