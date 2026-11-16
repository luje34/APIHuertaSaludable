package com.example.huertasaludable.api.reporte;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes-problemas")
public class ReporteProblemaController {
	private final ReporteProblemaService service;

	public ReporteProblemaController(ReporteProblemaService service) {
		this.service = service;
	}

	@GetMapping
	public List<ReporteProblemaResponse> listar(Authentication authentication) {
		return service.listar(authentication.getName());
	}

	@GetMapping("/{id}")
	public ReporteProblemaResponse buscar(@PathVariable Long id, Authentication authentication) {
		return service.buscar(id, authentication.getName());
	}

	@PostMapping
	public ResponseEntity<ReporteProblemaResponse> crear(
			Authentication authentication, @Valid @RequestBody ReporteProblemaCreateRequest request) {
		ReporteProblemaResponse creado = service.crear(authentication.getName(), request);
		return ResponseEntity.created(URI.create("/api/reportes-problemas/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public ReporteProblemaResponse actualizar(
			@PathVariable Long id, @Valid @RequestBody ReporteProblemaUpdateRequest request) {
		return service.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
