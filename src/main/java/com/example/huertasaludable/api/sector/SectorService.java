package com.example.huertasaludable.api.sector;

import java.util.List;

import com.example.huertasaludable.api.error.RecursoNoEncontradoException;
import com.example.huertasaludable.api.usuario.Usuario;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SectorService {
	private final SectorRepository sectores;
	private final UsuarioRepository usuarios;

	public SectorService(SectorRepository sectores, UsuarioRepository usuarios) {
		this.sectores = sectores;
		this.usuarios = usuarios;
	}

	@Transactional(readOnly = true)
	public List<SectorResponse> listar() {
		return sectores.findAll().stream().map(SectorResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public SectorResponse buscar(Long id) {
		return SectorResponse.from(obtener(id));
	}

	public SectorResponse crear(SectorRequest request) {
		return guardar(new Sector(), request);
	}

	public SectorResponse actualizar(Long id, SectorRequest request) {
		return guardar(obtener(id), request);
	}

	public void eliminar(Long id) {
		sectores.delete(obtener(id));
	}

	private SectorResponse guardar(Sector sector, SectorRequest request) {
		sector.setCodigo(request.codigo().trim());
		sector.setNombre(request.nombre().trim());
		sector.setDescripcion(request.descripcion());
		sector.setUbicacion(request.ubicacion());
		sector.setEstado(request.estado());
		sector.setResponsable(request.responsableId() == null ? null : obtenerUsuario(request.responsableId()));
		return SectorResponse.from(sectores.save(sector));
	}

	private Sector obtener(Long id) {
		return sectores.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sector", id));
	}

	private Usuario obtenerUsuario(Long id) {
		return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
	}
}
