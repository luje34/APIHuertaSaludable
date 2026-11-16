package com.example.huertasaludable.api.tarea;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.example.huertasaludable.api.cultivo.Cultivo;
import com.example.huertasaludable.api.cultivo.CultivoRepository;
import com.example.huertasaludable.api.cultivo.EstadoCultivo;
import com.example.huertasaludable.api.reporte.ReporteProblemaRepository;
import com.example.huertasaludable.api.sector.EstadoSector;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.sector.SectorRepository;
import com.example.huertasaludable.api.tarea.Tarea;
import com.example.huertasaludable.api.usuario.Rol;
import com.example.huertasaludable.api.usuario.Usuario;
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
class TareaSecurityTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TareaRepository tareas;
	@Autowired
	private ReporteProblemaRepository reportes;
	@Autowired
	private CultivoRepository cultivos;
	@Autowired
	private SectorRepository sectores;
	@Autowired
	private UsuarioRepository usuarios;

	private Usuario admin;
	private Usuario ana;
	private Usuario otra;
	private Sector sector;

	@BeforeEach
	void prepararDatos() {
		tareas.deleteAll();
		reportes.deleteAll();
		cultivos.deleteAll();
		sectores.deleteAll();
		usuarios.deleteAll();
		admin = usuario("admin@huerta.cl", Rol.ADMIN);
		ana = usuario("ana@huerta.cl", Rol.COLABORADOR);
		otra = usuario("otra@huerta.cl", Rol.COLABORADOR);
		sector = new Sector();
		sector.setCodigo("S-1");
		sector.setNombre("Invernadero");
		sector.setEstado(EstadoSector.DISPONIBLE);
		sector = sectores.save(sector);
	}

	@Test
	@WithMockUser(username = "ana@huerta.cl", roles = "COLABORADOR")
	void colaboradorSoloPuedeVerSusTareas() throws Exception {
		Tarea propia = tarea(ana);
		Tarea ajena = tarea(otra);

		mockMvc.perform(get("/api/tareas/{id}", propia.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estudianteId", is(ana.getId().intValue())));
		mockMvc.perform(get("/api/tareas/{id}", ajena.getId()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void rechazaTareaConCultivoDeOtroSector() throws Exception {
		Sector otroSector = new Sector();
		otroSector.setCodigo("S-2");
		otroSector.setNombre("Patio");
		otroSector.setEstado(EstadoSector.DISPONIBLE);
		otroSector = sectores.save(otroSector);
		Cultivo cultivo = new Cultivo();
		cultivo.setNombre("Tomate");
		cultivo.setSector(sector);
		cultivo.setFrecuenciaRiegoDias(2);
		cultivo.setEstado(EstadoCultivo.SEMBRADO);
		cultivo = cultivos.save(cultivo);
		String json = """
				{"tipo":"Riego","descripcion":"Regar","sectorId":%d,"cultivoId":%d,
				"fechaProgramada":"2026-11-17","estudianteId":%d,"docenteCreadorId":%d,"estado":"PENDIENTE"}
				""".formatted(otroSector.getId(), cultivo.getId(), ana.getId(), admin.getId());

		mockMvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void permiteSectorNuloYDerivaElSectorDelCultivo() throws Exception {
		Cultivo cultivo = new Cultivo();
		cultivo.setNombre("Tomate");
		cultivo.setSector(sector);
		cultivo.setFrecuenciaRiegoDias(2);
		cultivo.setEstado(EstadoCultivo.SEMBRADO);
		cultivo = cultivos.save(cultivo);
		String conCultivo = """
				{"tipo":"Riego","descripcion":"Regar","cultivoId":%d,"fechaProgramada":"2026-11-17",
				"estudianteId":%d,"docenteCreadorId":%d,"estado":"PENDIENTE"}
				""".formatted(cultivo.getId(), ana.getId(), admin.getId());
		mockMvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(conCultivo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sectorId", is(sector.getId().intValue())));

		String sinSectorNiCultivo = """
				{"tipo":"Riego","descripcion":"Regar","sectorId":null,"cultivoId":null,"fechaProgramada":"2026-11-17",
				"estudianteId":%d,"docenteCreadorId":%d,"estado":"PENDIENTE"}
				""".formatted(ana.getId(), admin.getId());
		mockMvc.perform(post("/api/tareas").contentType(MediaType.APPLICATION_JSON).content(sinSectorNiCultivo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sectorId").value(nullValue()));
	}

	private Usuario usuario(String email, Rol rol) {
		Usuario usuario = new Usuario();
		usuario.setNombre(email);
		usuario.setEmail(email);
		usuario.setPassword("hash");
		usuario.setRol(rol);
		usuario.setTallerGrupo("Taller");
		return usuarios.save(usuario);
	}

	private Tarea tarea(Usuario estudiante) {
		Tarea tarea = new Tarea();
		tarea.setTipo("Riego");
		tarea.setDescripcion("Regar el sector");
		tarea.setSector(sector);
		tarea.setFechaProgramada(LocalDate.now());
		tarea.setEstudiante(estudiante);
		tarea.setDocenteCreador(admin);
		tarea.setEstado(EstadoTarea.PENDIENTE);
		return tareas.save(tarea);
	}
}
