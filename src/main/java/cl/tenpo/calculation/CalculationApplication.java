package cl.tenpo.calculation;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicación principal de Spring Boot para el servicio de cálculo.
 */
@SpringBootApplication
@EnableAsync
public class CalculationApplication {
	public static void main(String[] args) {
		SpringApplication.run(CalculationApplication.class, args);
	}
}

