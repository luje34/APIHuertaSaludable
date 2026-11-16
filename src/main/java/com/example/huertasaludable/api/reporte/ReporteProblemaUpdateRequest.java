package com.example.huertasaludable.api.reporte;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReporteProblemaUpdateRequest(
		Long sectorId,
		Long cultivoId,
		@NotBlank @Size(max = 100) String tipo,
		@NotBlank @Size(max = 1600) String descripcion,
		@NotNull LocalDate fechaReporte,
		@NotNull Long usuarioReportadorId,
		@Size(max = 2048) String fotografiaUri,
		@NotNull EstadoReporteProblema estado,
		@Size(max = 1500) String comentarioDocente,
		LocalDate fechaAtencion) {
}
