package com.example.huertasaludable.api.tarea;

import java.util.List;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.example.huertasaludable.api.cultivo.Cultivo;
import com.example.huertasaludable.api.cultivo.CultivoRepository;
import com.example.huertasaludable.api.error.RecursoNoEncontradoException;
import com.example.huertasaludable.api.error.ReglaNegocioException;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.sector.SectorRepository;
import com.example.huertasaludable.api.usuario.Rol;
import com.example.huertasaludable.api.usuario.Usuario;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TareaService {
	private final TareaRepository tareas;
	private final SectorRepository sectores;
	private final CultivoRepository cultivos;
	private final UsuarioRepository usuarios;

	public TareaService(TareaRepository tareas, SectorRepository sectores, CultivoRepository cultivos,
			UsuarioRepository usuarios) {
		this.tareas = tareas;
		this.sectores = sectores;
		this.cultivos = cultivos;
		this.usuarios = usuarios;
	}

	@Transactional(readOnly = true)
	public List<TareaResponse> listar(String email) {
		Usuario actual = usuarioActual(email);
		List<Tarea> resultado = actual.getRol() == Rol.ADMIN
				? tareas.findAll()
				: tareas.findByEstudiante_IdOrderByFechaProgramadaAsc(actual.getId());
		return resultado.stream().map(TareaResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public TareaResponse buscar(Long id, String email) {
		Usuario actual = usuarioActual(email);
		Tarea tarea = obtener(id);
		verificarPropiedad(tarea, actual);
		return TareaResponse.from(tarea);
	}

	public TareaResponse crear(TareaRequest request) {
		Tarea tarea = new Tarea();
		aplicar(tarea, request);
		return TareaResponse.from(tareas.save(tarea));
	}

	public TareaResponse actualizar(Long id, TareaRequest request) {
		Tarea tarea = obtener(id);
		aplicar(tarea, request);
		return TareaResponse.from(tareas.save(tarea));
	}

	public TareaResponse actualizarAvance(Long id, String email, TareaAvanceRequest request) {
		Usuario actual = usuarioActual(email);
		Tarea tarea = obtener(id);
		verificarPropiedad(tarea, actual);
		if (actual.getRol() != Rol.COLABORADOR) {
			throw new ReglaNegocioException("Este endpoint es para el avance de tareas del colaborador");
		}
		if (request.estado() != EstadoTarea.EN_DESARROLLO
				&& request.estado() != EstadoTarea.PENDIENTE_VALIDACION) {
			throw new ReglaNegocioException("El colaborador solo puede iniciar una tarea o solicitar validación");
		}
		if (tarea.getEstado() == EstadoTarea.VALIDADA || tarea.getEstado() == EstadoTarea.RECHAZADA) {
			throw new ReglaNegocioException("La tarea ya fue cerrada por un docente");
		}
		tarea.setEstado(request.estado());
		tarea.setComentarioEstudiante(request.comentarioEstudiante());
		tarea.setFotografiaEvidenciaUri(request.fotografiaEvidenciaUri());
		if (request.estado() == EstadoTarea.EN_DESARROLLO && tarea.getFechaRealizacion() == null) {
			tarea.setFechaRealizacion(LocalDate.now(ZoneOffset.UTC));
		}
		return TareaResponse.from(tareas.save(tarea));
	}

	public void eliminar(Long id) {
		tareas.delete(obtener(id));
	}

	private void aplicar(Tarea tarea, TareaRequest request) {
		Cultivo cultivo = request.cultivoId() == null ? null : cultivos.findById(request.cultivoId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Cultivo", request.cultivoId()));
		Sector sector = request.sectorId() == null ? null : sectores.findById(request.sectorId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Sector", request.sectorId()));
		if (cultivo != null && sector != null && !cultivo.getSector().getId().equals(sector.getId())) {
			throw new ReglaNegocioException("El cultivo debe pertenecer al sector indicado");
		}
		if (sector == null && cultivo != null) {
			sector = cultivo.getSector();
		}
		Usuario estudiante = obtenerUsuario(request.estudianteId());
		Usuario creador = obtenerUsuario(request.docenteCreadorId());
		Usuario validador = request.docenteValidadorId() == null ? null : obtenerUsuario(request.docenteValidadorId());
		if (estudiante.getRol() != Rol.COLABORADOR || creador.getRol() != Rol.ADMIN
				|| (validador != null && validador.getRol() != Rol.ADMIN)) {
			throw new ReglaNegocioException("La tarea requiere colaborador asignado y docentes administradores");
		}
		tarea.setTipo(request.tipo().trim());
		tarea.setDescripcion(request.descripcion().trim());
		tarea.setSector(sector);
		tarea.setCultivo(cultivo);
		tarea.setFechaProgramada(request.fechaProgramada());
		tarea.setFechaRealizacion(request.fechaRealizacion());
		tarea.setEstudiante(estudiante);
		tarea.setDocenteCreador(creador);
		tarea.setEstado(request.estado());
		tarea.setComentarioEstudiante(request.comentarioEstudiante());
		tarea.setComentarioDocente(request.comentarioDocente());
		tarea.setFotografiaEvidenciaUri(request.fotografiaEvidenciaUri());
		tarea.setFechaValidacion(request.fechaValidacion());
		tarea.setDocenteValidador(validador);
	}

	private Tarea obtener(Long id) {
		return tareas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Tarea", id));
	}

	private Usuario obtenerUsuario(Long id) {
		return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
	}

	private Usuario usuarioActual(String email) {
		return usuarios.findByEmailIgnoreCase(email)
				.filter(Usuario::isActivo)
				.orElseThrow(() -> new ReglaNegocioException("La cuenta está inactiva o no existe"));
	}

	private void verificarPropiedad(Tarea tarea, Usuario actual) {
		if (actual.getRol() != Rol.ADMIN && !tarea.getEstudiante().getId().equals(actual.getId())) {
			throw new org.springframework.security.access.AccessDeniedException("No puedes consultar esta tarea");
		}
	}
}
