package cl.tenpo.calculation.dto;

import java.util.List;

/**
 * DTO para paginación de resultados.
 * 
 * @param <T> el tipo de contenido de la página
 */
public record PageDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
