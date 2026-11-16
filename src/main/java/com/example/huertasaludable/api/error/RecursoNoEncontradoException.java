package com.example.huertasaludable.api.error;

public class RecursoNoEncontradoException extends RuntimeException {

	public RecursoNoEncontradoException(String recurso, Long id) {
		super(recurso + " " + id + " no existe");
	}
}
