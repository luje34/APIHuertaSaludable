package com.example.huertasaludable.api.tarea;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {
	private final TareaService service;

	public TareaController(TareaService service) {
		this.service = service;
	}

	@GetMapping
	public List<TareaResponse> listar(Authentication authentication) {
		return service.listar(authentication.getName());
	}

	@GetMapping("/{id}")
	public TareaResponse buscar(@PathVariable Long id, Authentication authentication) {
		return service.buscar(id, authentication.getName());
	}

	@PostMapping
	public ResponseEntity<TareaResponse> crear(@Valid @RequestBody TareaRequest request) {
		TareaResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/tareas/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public TareaResponse actualizar(@PathVariable Long id, @Valid @RequestBody TareaRequest request) {
		return service.actualizar(id, request);
	}

	@PatchMapping("/{id}/avance")
	public TareaResponse actualizarAvance(
			@PathVariable Long id, Authentication authentication, @Valid @RequestBody TareaAvanceRequest request) {
		return service.actualizarAvance(id, authentication.getName(), request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
