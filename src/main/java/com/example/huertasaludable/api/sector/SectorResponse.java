package com.example.huertasaludable.api.sector;

import java.time.LocalDateTime;

public record SectorResponse(
		Long id, String codigo, String nombre, String descripcion, String ubicacion,
		EstadoSector estado, Long responsableId, LocalDateTime fechaCreacion) {

	public static SectorResponse from(Sector sector) {
		return new SectorResponse(
				sector.getId(), sector.getCodigo(), sector.getNombre(), sector.getDescripcion(),
				sector.getUbicacion(), sector.getEstado(),
				sector.getResponsable() == null ? null : sector.getResponsable().getId(),
				sector.getFechaCreacion());
	}
}
