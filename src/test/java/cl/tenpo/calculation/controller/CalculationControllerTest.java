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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.tenpo.calculation.service.AuditService;
import cl.tenpo.calculation.service.CalculationService;

/**
 * Test unitarios para el controlador {@link CalculationController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CalculationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculationService calculationService;
    
    @MockitoBean
    private AuditService auditService;

    /**
     * Test para verificar que el controlador de cálculo retorna el resultado correcto
     * cuando los parámetros son válidos.
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("Debería retornar el resultado correcto cuando los parámetros son válidos")
    void shouldReturnCalculatedValue() throws Exception {
        when(calculationService.calculate(5, 5)).thenReturn(new BigDecimal(11));

        mockMvc.perform(get("/calculation")
                        .param("num1", "5")
                        .param("num2", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("11"));
    }

    /**
     * Test para verificar que el controlador retorna un error 400
     * cuando uno de los parámetros es inválido (no numérico).
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("Debería retornar 400 si uno de los parámetros es inválido (no numérico)")
    void shouldReturn400OnInvalidNumber() throws Exception {
        mockMvc.perform(get("/calculation")
                        .param("num1", "abc")
                        .param("num2", "5"))
                .andExpect(status().isBadRequest());
    }

    /** 
     * Test para verificar que el controlador retorna un error 400
     * cuando falta un parámetro requerido.	
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("Debería retornar 400 si falta un parámetro")
    void shouldReturn400WhenMissingParam() throws Exception {
        mockMvc.perform(get("/calculation")
                        .param("num1", "5"))
                .andExpect(status().isBadRequest());
    }
}
