package com.example.huertasaludable.api.reporte;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteProblemaRepository extends JpaRepository<ReporteProblema, Long> {
	List<ReporteProblema> findByUsuarioReportador_IdOrderByFechaReporteDesc(Long usuarioId);
}
