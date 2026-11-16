package com.example.huertasaludable.api.cultivo;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {
	private final CultivoService service;

	public CultivoController(CultivoService service) {
		this.service = service;
	}

	@GetMapping
	public List<CultivoResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public CultivoResponse buscar(@PathVariable Long id) {
		return service.buscar(id);
	}

	@PostMapping
	public ResponseEntity<CultivoResponse> crear(@Valid @RequestBody CultivoRequest request) {
		CultivoResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/cultivos/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public CultivoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CultivoRequest request) {
		return service.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
