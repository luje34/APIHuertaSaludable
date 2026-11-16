package com.example.huertasaludable.api.usuario;

public class UsuarioNoEncontradoException extends RuntimeException {

	public UsuarioNoEncontradoException(Long id) {
		super("No existe un usuario con id " + id);
	}

	public UsuarioNoEncontradoException() {
		super("El usuario autenticado no existe");
	}
}
