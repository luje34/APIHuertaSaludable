package com.example.huertasaludable.api.tarea;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TareaResponse(
		Long id, String tipo, String descripcion, Long sectorId, Long cultivoId,
		LocalDate fechaProgramada, LocalDate fechaRealizacion,
		Long estudianteId, Long docenteCreadorId, EstadoTarea estado,
		String comentarioEstudiante, String comentarioDocente, String fotografiaEvidenciaUri,
		LocalDateTime fechaValidacion, Long docenteValidadorId) {

	public static TareaResponse from(Tarea tarea) {
		return new TareaResponse(
				tarea.getId(), tarea.getTipo(), tarea.getDescripcion(),
				tarea.getSector() == null ? null : tarea.getSector().getId(),
				tarea.getCultivo() == null ? null : tarea.getCultivo().getId(),
				tarea.getFechaProgramada(), tarea.getFechaRealizacion(),
				tarea.getEstudiante().getId(), tarea.getDocenteCreador().getId(), tarea.getEstado(),
				tarea.getComentarioEstudiante(), tarea.getComentarioDocente(),
				tarea.getFotografiaEvidenciaUri(), tarea.getFechaValidacion(),
				tarea.getDocenteValidador() == null ? null : tarea.getDocenteValidador().getId());
	}
}
