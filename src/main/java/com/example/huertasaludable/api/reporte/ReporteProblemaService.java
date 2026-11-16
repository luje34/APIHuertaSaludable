package com.example.huertasaludable.api.reporte;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.example.huertasaludable.api.cultivo.Cultivo;
import com.example.huertasaludable.api.cultivo.CultivoRepository;
import com.example.huertasaludable.api.error.RecursoNoEncontradoException;
import com.example.huertasaludable.api.error.ReglaNegocioException;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.sector.SectorRepository;
import com.example.huertasaludable.api.usuario.Rol;
import com.example.huertasaludable.api.usuario.Usuario;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReporteProblemaService {
	private final ReporteProblemaRepository reportes;
	private final SectorRepository sectores;
	private final CultivoRepository cultivos;
	private final UsuarioRepository usuarios;

	public ReporteProblemaService(ReporteProblemaRepository reportes, SectorRepository sectores,
			CultivoRepository cultivos, UsuarioRepository usuarios) {
		this.reportes = reportes;
		this.sectores = sectores;
		this.cultivos = cultivos;
		this.usuarios = usuarios;
	}

	@Transactional(readOnly = true)
	public List<ReporteProblemaResponse> listar(String email) {
		Usuario actual = usuarioActual(email);
		List<ReporteProblema> resultado = actual.getRol() == Rol.ADMIN
				? reportes.findAll()
				: reportes.findByUsuarioReportador_IdOrderByFechaReporteDesc(actual.getId());
		return resultado.stream().map(ReporteProblemaResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public ReporteProblemaResponse buscar(Long id, String email) {
		Usuario actual = usuarioActual(email);
		ReporteProblema reporte = obtener(id);
		verificarPropiedad(reporte, actual);
		return ReporteProblemaResponse.from(reporte);
	}

	public ReporteProblemaResponse crear(String email, ReporteProblemaCreateRequest request) {
		Usuario actual = usuarioActual(email);
		Cultivo cultivo = obtenerCultivo(request.cultivoId());
		Sector sector = resolverSector(request.sectorId(), cultivo);
		ReporteProblema reporte = new ReporteProblema();
		reporte.setSector(sector);
		reporte.setCultivo(cultivo);
		reporte.setTipo(request.tipo().trim());
		reporte.setDescripcion(request.descripcion().trim());
		reporte.setFechaReporte(LocalDate.now(ZoneOffset.UTC));
		reporte.setUsuarioReportador(actual);
		reporte.setFotografiaUri(request.fotografiaUri());
		reporte.setEstado(EstadoReporteProblema.INFORMADO);
		return ReporteProblemaResponse.from(reportes.save(reporte));
	}

	public ReporteProblemaResponse actualizar(Long id, ReporteProblemaUpdateRequest request) {
		ReporteProblema reporte = obtener(id);
		Cultivo cultivo = obtenerCultivo(request.cultivoId());
		Sector sector = resolverSector(request.sectorId(), cultivo);
		Usuario reportador = usuarios.findById(request.usuarioReportadorId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario", request.usuarioReportadorId()));
		reporte.setSector(sector);
		reporte.setCultivo(cultivo);
		reporte.setTipo(request.tipo().trim());
		reporte.setDescripcion(request.descripcion().trim());
		reporte.setFechaReporte(request.fechaReporte());
		reporte.setUsuarioReportador(reportador);
		reporte.setFotografiaUri(request.fotografiaUri());
		reporte.setEstado(request.estado());
		reporte.setComentarioDocente(request.comentarioDocente());
		reporte.setFechaAtencion(request.fechaAtencion());
		return ReporteProblemaResponse.from(reportes.save(reporte));
	}

	public void eliminar(Long id) {
		reportes.delete(obtener(id));
	}

	private Sector resolverSector(Long sectorId, Cultivo cultivo) {
		Sector sector = sectorId == null ? null : obtenerSector(sectorId);
		if (cultivo != null && sector != null && !cultivo.getSector().getId().equals(sector.getId())) {
			throw new ReglaNegocioException("El cultivo debe pertenecer al sector indicado");
		}
		return sector == null && cultivo != null ? cultivo.getSector() : sector;
	}

	private Cultivo obtenerCultivo(Long id) {
		return id == null ? null : cultivos.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Cultivo", id));
	}

	private Sector obtenerSector(Long id) {
		return sectores.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sector", id));
	}

	private ReporteProblema obtener(Long id) {
		return reportes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Reporte", id));
	}

	private Usuario usuarioActual(String email) {
		return usuarios.findByEmailIgnoreCase(email).filter(Usuario::isActivo)
				.orElseThrow(() -> new ReglaNegocioException("La cuenta está inactiva o no existe"));
	}

	private void verificarPropiedad(ReporteProblema reporte, Usuario actual) {
		if (actual.getRol() != Rol.ADMIN && !reporte.getUsuarioReportador().getId().equals(actual.getId())) {
			throw new AccessDeniedException("No puedes consultar este reporte");
		}
	}
}
