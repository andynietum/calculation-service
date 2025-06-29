package cl.tenpo.calculation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.tenpo.calculation.dto.PageDto;
import cl.tenpo.calculation.dto.RequestAuditDto;
import cl.tenpo.calculation.service.AuditService;

/**
 * Test unitarios para el controlador {@link AuditController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

	/**
	 * Test para verificar que el controlador de auditoría retorna correctamente la lista paginada de auditorías.
	 * 
	 * @throws Exception si ocurre algún error durante la ejecución del test.
	 */
    @Test
    @DisplayName("Debería retornar correctamente la lista paginada de auditorías")
    void shouldReturnPagedAuditSuccessfully() throws Exception {
        List<RequestAuditDto> audits = List.of(
                new RequestAuditDto(LocalDateTime.now(), "GET /calculate", "[5,5]", "11", true),
                new RequestAuditDto(LocalDateTime.now(), "GET /calculate", "[5,5]", "Error X", false)
        );
        PageDto<RequestAuditDto> pageDto = new PageDto<>(audits, 0, 10, 2l, 1, true);

        when(auditService.getAll(0, 10)).thenReturn(pageDto);

        mockMvc.perform(get("/audit")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    /**	
     * Test para verificar que el controlador retorna un error 400 cuando los parámetros de paginación son inválidos.
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("Debería retornar 400 Bad Request cuando los parámetros de paginación son inválidos")
    void shouldReturnBadRequestWhenParamsAreInvalid() throws Exception {
        mockMvc.perform(get("/audit")
                        .param("page", "-1")  // inválido por @Min(0)
                        .param("size", "-10")) // inválido también
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

	/**
	 * Test para verificar que el controlador retorna un error 500 cuando ocurre un fallo inesperado en el servicio.
	 * 
	 * @throws Exception si ocurre algún error durante la ejecución del test.
	 */
    @Test
    @DisplayName("Debería retornar 500 Internal Server Error cuando ocurre un fallo inesperado en el servicio")
    void shouldReturnInternalServerErrorOnServiceFailure() throws Exception {
        when(auditService.getAll(0, 10)).thenThrow(new RuntimeException("Falla inesperada"));

        mockMvc.perform(get("/audit")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}

