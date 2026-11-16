package com.example.huertasaludable.api.tarea;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TareaAvanceRequest(
		@NotNull EstadoTarea estado,
		@Size(max = 1500) String comentarioEstudiante,
		@Size(max = 2048) String fotografiaEvidenciaUri) {
}
