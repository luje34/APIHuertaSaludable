package com.example.huertasaludable.api.tarea;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TareaRequest(
		@NotBlank @Size(max = 100) String tipo,
		@NotBlank @Size(max = 1200) String descripcion,
		Long sectorId,
		Long cultivoId,
		@NotNull LocalDate fechaProgramada,
		LocalDate fechaRealizacion,
		@NotNull Long estudianteId,
		@NotNull Long docenteCreadorId,
		@NotNull EstadoTarea estado,
		@Size(max = 1500) String comentarioEstudiante,
		@Size(max = 1500) String comentarioDocente,
		@Size(max = 2048) String fotografiaEvidenciaUri,
		LocalDateTime fechaValidacion,
		Long docenteValidadorId) {
}
