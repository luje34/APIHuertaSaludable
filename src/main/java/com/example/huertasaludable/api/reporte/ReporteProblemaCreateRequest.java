package com.example.huertasaludable.api.reporte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReporteProblemaCreateRequest(
		Long sectorId,
		Long cultivoId,
		@NotBlank @Size(max = 100) String tipo,
		@NotBlank @Size(max = 1600) String descripcion,
		@Size(max = 2048) String fotografiaUri) {
}
