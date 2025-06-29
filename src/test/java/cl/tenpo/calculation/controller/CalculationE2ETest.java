package cl.tenpo.calculation.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import cl.tenpo.calculation.entity.RequestAudit;
import cl.tenpo.calculation.repository.RequestAuditRepository;
import cl.tenpo.calculation.service.PercentageService;

/**
 * Test de extremo a extremo (E2E) para el endpoint de cálculo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CalculationE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "externalPercentageService")
    private PercentageService percentageService; // el remoto (fallará en un caso)
    
    @Autowired
    private RequestAuditRepository requestAuditRepository;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /** 
     * Verifica que se calcula correctamente el resultado cuando el servicio remoto funciona.
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("E2E: devuelve el resultado correcto cuando el servicio remoto funciona")
    void testCalculate_SuccessfulRemoteCall() throws Exception {
        // 10% de 10 = 1 → resultado = 11.0
        when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(10));

        this.performGetCalculation("5", "5")
                .andExpect(status().isOk())
                .andExpect(content().string("11"));

        verify(percentageService, times(1)).getPercentage();
        verify(valueOperations).set("percentage", "10", Duration.ofMinutes(1));
    }

    /**
     * Verifica que se usa la cache cuando el servicio remoto funciona.
     * 
     * @throws Exception
     */
    @Test
    @DisplayName("E2E: usa cache cuando el servicio remoto falla")
    void testCalculate_UsesCacheWhenRemoteFails() throws Exception {
        // Simulamos caída total del servicio remoto
        when(percentageService.getPercentage()).thenThrow(new RuntimeException("error remoto"));
        when(valueOperations.get("percentage")).thenReturn("20");

        // 20% de 10 = 2 → resultado = 12.0
        this.performGetCalculation("5", "5")
                .andExpect(status().isOk())
                .andExpect(content().string("12"));

        verify(percentageService, times(3)).getPercentage(); // Retry 3 veces
        verify(valueOperations).get("percentage");
    }

    /**
     * Verifica que se devuelve 503 si el servicio remoto falla y no hay cache.
     * 
     * @throws Exception si ocurre algún error durante la ejecución del test.
     */
    @Test
    @DisplayName("E2E: devuelve 503 si el servicio remoto y la cache fallan")
    void testCalculate_Throws503WhenAllFail() throws Exception {
        when(percentageService.getPercentage()).thenThrow(new RuntimeException("total failure"));
        when(valueOperations.get("percentage")).thenReturn(null); // sin cache

        this.performGetCalculation("3", "2")
                .andExpect(status().isServiceUnavailable());

        verify(percentageService, times(3)).getPercentage();
        verify(valueOperations).get("percentage");
    }

    /**
     * Verifica que se devuelve 400 si falta un parámetro requerido.
     * 
     * @throws Exception
     */
    @Test
    @DisplayName("E2E: devuelve 400 si se envía un parámetro inválido")
    void testCalculate_InvalidParam() throws Exception {
    	this.performGetCalculation("abc", "5")
            .andExpect(status().isBadRequest());
    }   
    
    /**
     * Verifica que se registra correctamente la auditoría cuando el request es exitoso.	
     * 
     * @throws Exception
     */
    @Test
    @DisplayName("E2E Audit: éxito del endpoint y se registra auditoría")
    void testAuditSuccess() throws Exception {
    	requestAuditRepository.deleteAll();
    	
        when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(10));

        this.performGetCalculation("5", "5")
                .andExpect(status().isOk())
                .andExpect(content().string("11"));

        List<RequestAudit> logs = requestAuditRepository.findAll();
        assertEquals(1, logs.size());

        RequestAudit log = logs.get(0);
        assertEquals("GET /calculation", log.getEndpoint());
        assertTrue(log.getIncoming().contains("5"));
        assertTrue(log.getResult().contains("11"));
        assertTrue(log.isSuccess());
    }  
    
    /**
     * Realiza la petición GET al endpoint de cálculo con los parámetros especificados.
     * 
     * @param num1 Primero parametro numérico
     * @param num2 Segundo parametro numérico
     * @return ResultActions con el resultado de la petición
     * @throws Exception si ocurre algún error durante la ejecución de la petición
     */
    private ResultActions performGetCalculation(String num1, String num2) throws Exception {
		return mockMvc.perform(get("/calculation")
				.param("num1", num1)
				.param("num2", num2));
	}
    
    /**
     * Clase de test para verificar el comportamiento de auditoría
     * cuando el registro falla pero el request responde correctamente.
     */
    @Nested
    class AuditFailureTest {
        
        @MockitoSpyBean
        private RequestAuditRepository spyRepository;

        /**
         * Verifica que el registro de auditoría falla pero el endpoint responde correctamente.
         * 
         * @throws Exception si ocurre algún error durante la ejecución del test.
         */
        @Test
        @DisplayName("E2E Audit: el log falla pero el requeset responde correctamente")
        void testAuditFailsButEndpointSucceeds() throws Exception {
           when(percentageService.getPercentage()).thenReturn(BigDecimal.valueOf(10));
           doThrow(new RuntimeException("fallo de auditoría"))
					.when(spyRepository).save(any(RequestAudit.class));
        	
        	performGetCalculation("1", "9")
					.andExpect(status().isOk())
					.andExpect(content().string("11"));
        }
    }
}