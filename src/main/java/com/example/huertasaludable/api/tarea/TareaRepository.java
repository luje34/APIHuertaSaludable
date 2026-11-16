package com.example.huertasaludable.api.tarea;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
	List<Tarea> findByEstudiante_IdOrderByFechaProgramadaAsc(Long estudianteId);
}
