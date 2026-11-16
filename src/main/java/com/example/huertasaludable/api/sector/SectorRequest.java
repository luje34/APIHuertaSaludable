package com.example.huertasaludable.api.sector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SectorRequest(
		@NotBlank @Size(max = 40) String codigo,
		@NotBlank @Size(max = 100) String nombre,
		@Size(max = 500) String descripcion,
		@Size(max = 180) String ubicacion,
		@NotNull EstadoSector estado,
		Long responsableId) {
}
