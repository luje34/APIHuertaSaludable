package com.example.huertasaludable.api.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioSelfUpdateRequest(
		@NotBlank @Size(max = 100) String nombre,
		@NotBlank @Email @Size(max = 180) String email,
		@Size(min = 10, max = 72) String password,
		@NotBlank @Size(max = 120) String tallerGrupo) {
}
