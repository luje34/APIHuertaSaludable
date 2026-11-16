package com.example.huertasaludable.api.reporte;

import java.time.LocalDate;

public record ReporteProblemaResponse(
		Long id, Long sectorId, Long cultivoId, String tipo, String descripcion, LocalDate fechaReporte,
		Long usuarioReportadorId, String fotografiaUri, EstadoReporteProblema estado,
		String comentarioDocente, LocalDate fechaAtencion) {

	public static ReporteProblemaResponse from(ReporteProblema reporte) {
		return new ReporteProblemaResponse(
				reporte.getId(), reporte.getSector() == null ? null : reporte.getSector().getId(),
				reporte.getCultivo() == null ? null : reporte.getCultivo().getId(),
				reporte.getTipo(), reporte.getDescripcion(), reporte.getFechaReporte(),
				reporte.getUsuarioReportador().getId(), reporte.getFotografiaUri(),
				reporte.getEstado(), reporte.getComentarioDocente(), reporte.getFechaAtencion());
	}
}
