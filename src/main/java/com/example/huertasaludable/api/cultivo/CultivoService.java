package com.example.huertasaludable.api.cultivo;

import java.util.List;

import com.example.huertasaludable.api.error.RecursoNoEncontradoException;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.sector.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CultivoService {
	private final CultivoRepository cultivos;
	private final SectorRepository sectores;

	public CultivoService(CultivoRepository cultivos, SectorRepository sectores) {
		this.cultivos = cultivos;
		this.sectores = sectores;
	}

	@Transactional(readOnly = true)
	public List<CultivoResponse> listar() {
		return cultivos.findAll().stream().map(CultivoResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public CultivoResponse buscar(Long id) {
		return CultivoResponse.from(obtener(id));
	}

	public CultivoResponse crear(CultivoRequest request) {
		return guardar(new Cultivo(), request);
	}

	public CultivoResponse actualizar(Long id, CultivoRequest request) {
		return guardar(obtener(id), request);
	}

	public void eliminar(Long id) {
		cultivos.delete(obtener(id));
	}

	private CultivoResponse guardar(Cultivo cultivo, CultivoRequest request) {
		Sector sector = sectores.findById(request.sectorId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Sector", request.sectorId()));
		cultivo.setNombre(request.nombre().trim());
		cultivo.setDescripcion(request.descripcion());
		cultivo.setSector(sector);
		cultivo.setFechaSiembra(request.fechaSiembra());
		cultivo.setFechaTrasplante(request.fechaTrasplante());
		cultivo.setFechaEstimadaCosecha(request.fechaEstimadaCosecha());
		cultivo.setFrecuenciaRiegoDias(request.frecuenciaRiegoDias());
		cultivo.setEstado(request.estado());
		cultivo.setObservaciones(request.observaciones());
		cultivo.setFotografiaUri(request.fotografiaUri());
		return CultivoResponse.from(cultivos.save(cultivo));
	}

	private Cultivo obtener(Long id) {
		return cultivos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cultivo", id));
	}
}
