package com.example.huertasaludable.api.cultivo;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CultivoRequest(
		@NotBlank @Size(max = 120) String nombre,
		@Size(max = 1000) String descripcion,
		@NotNull Long sectorId,
		@NotNull LocalDate fechaSiembra,
		LocalDate fechaTrasplante,
		@NotNull LocalDate fechaEstimadaCosecha,
		@NotNull @Min(1) Integer frecuenciaRiegoDias,
		@NotNull EstadoCultivo estado,
		@Size(max = 1500) String observaciones,
		@Size(max = 2048) String fotografiaUri) {
}
