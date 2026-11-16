package com.example.huertasaludable.api.cultivo;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.huertasaludable.api.reporte.ReporteProblemaRepository;
import com.example.huertasaludable.api.sector.EstadoSector;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.sector.SectorRepository;
import com.example.huertasaludable.api.tarea.TareaRepository;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CultivoControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private CultivoRepository cultivos;
	@Autowired
	private TareaRepository tareas;
	@Autowired
	private ReporteProblemaRepository reportes;
	@Autowired
	private SectorRepository sectores;
	@Autowired
	private UsuarioRepository usuarios;

	private Sector sector;

	@BeforeEach
	void prepararDatos() {
		tareas.deleteAll();
		reportes.deleteAll();
		cultivos.deleteAll();
		sectores.deleteAll();
		usuarios.deleteAll();
		sector = new Sector();
		sector.setCodigo("S-1");
		sector.setNombre("Invernadero");
		sector.setEstado(EstadoSector.DISPONIBLE);
		sector = sectores.save(sector);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void requiereFechasDeSiembraYCosechaPeroNoTrasplante() throws Exception {
		mockMvc.perform(post("/api/cultivos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Tomate","sectorId":%d,"fechaEstimadaCosecha":"2026-12-10",
								"frecuenciaRiegoDias":2,"estado":"PLANIFICADO"}
								""".formatted(sector.getId())))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/cultivos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Tomate","sectorId":%d,"fechaSiembra":"2026-11-17",
								"frecuenciaRiegoDias":2,"estado":"PLANIFICADO"}
								""".formatted(sector.getId())))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/cultivos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Tomate","sectorId":%d,"fechaSiembra":"2026-11-17",
								"fechaEstimadaCosecha":"2026-12-10","frecuenciaRiegoDias":2,
								"estado":"PLANIFICADO"}
								""".formatted(sector.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.fechaSiembra", is("2026-11-17")))
				.andExpect(jsonPath("$.fechaEstimadaCosecha", is("2026-12-10")))
				.andExpect(jsonPath("$.fechaTrasplante").value(nullValue()));
	}
}
