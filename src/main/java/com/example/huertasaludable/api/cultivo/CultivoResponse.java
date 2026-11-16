package com.example.huertasaludable.api.cultivo;

import java.time.LocalDate;

public record CultivoResponse(
		Long id, String nombre, String descripcion, Long sectorId,
		LocalDate fechaSiembra, LocalDate fechaTrasplante, LocalDate fechaEstimadaCosecha,
		Integer frecuenciaRiegoDias, EstadoCultivo estado, String observaciones, String fotografiaUri) {

	public static CultivoResponse from(Cultivo cultivo) {
		return new CultivoResponse(
				cultivo.getId(), cultivo.getNombre(), cultivo.getDescripcion(), cultivo.getSector().getId(),
				cultivo.getFechaSiembra(), cultivo.getFechaTrasplante(), cultivo.getFechaEstimadaCosecha(),
				cultivo.getFrecuenciaRiegoDias(), cultivo.getEstado(), cultivo.getObservaciones(),
				cultivo.getFotografiaUri());
	}
}
