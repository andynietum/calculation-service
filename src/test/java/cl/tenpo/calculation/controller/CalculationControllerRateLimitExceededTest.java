package cl.tenpo.calculation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.tenpo.calculation.service.AuditService;
import cl.tenpo.calculation.service.CalculationService;

/**
 * Clase de test para verificar el límite de peticiones concurrentes
 * definido por el rate limiter de Resilience4j en {@link CalculationController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"test","rate-limit"})
public class CalculationControllerRateLimitExceededTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculationService calculationService;
    
    @MockitoBean
    private AuditService auditService;
    
    /**
     * Test para verificar que se retorna un error 429 (Too Many Requests)
     * cuando se excede el límite de 3 peticiones permitidas
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("Debería retornar HTTP 429 al exceder el límite de 3 requests permitidos")
    void testRateLimitExceeded() throws Exception {
        when(calculationService.calculate(5, 5)).thenReturn(new BigDecimal(11));

        // Hacemos 3 llamadas válidas (permitidas)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/calculation")
                            .param("num1", "5")
                            .param("num2", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("11"));
        }

        // La 4ta llamada debería ser bloqueada con 429 Too Many Requests
        mockMvc.perform(get("/calculation")
                        .param("num1", "5")
                        .param("num2", "5"))
                .andExpect(status().isTooManyRequests());
    }

}
