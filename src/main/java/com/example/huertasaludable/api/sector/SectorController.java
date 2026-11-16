package com.example.huertasaludable.api.sector;

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
@RequestMapping("/api/sectores")
public class SectorController {
	private final SectorService service;

	public SectorController(SectorService service) {
		this.service = service;
	}

	@GetMapping
	public List<SectorResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public SectorResponse buscar(@PathVariable Long id) {
		return service.buscar(id);
	}

	@PostMapping
	public ResponseEntity<SectorResponse> crear(@Valid @RequestBody SectorRequest request) {
		SectorResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/sectores/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public SectorResponse actualizar(@PathVariable Long id, @Valid @RequestBody SectorRequest request) {
		return service.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
