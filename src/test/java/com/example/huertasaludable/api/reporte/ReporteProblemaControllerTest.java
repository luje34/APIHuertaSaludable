package com.example.huertasaludable.api.reporte;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import com.example.huertasaludable.api.cultivo.Cultivo;
import com.example.huertasaludable.api.cultivo.CultivoRepository;
import com.example.huertasaludable.api.cultivo.EstadoCultivo;
import com.example.huertasaludable.api.sector.EstadoSector;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.sector.SectorRepository;
import com.example.huertasaludable.api.tarea.TareaRepository;
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
class ReporteProblemaControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ReporteProblemaRepository reportes;
	@Autowired
	private TareaRepository tareas;
	@Autowired
	private CultivoRepository cultivos;
	@Autowired
	private SectorRepository sectores;
	@Autowired
	private UsuarioRepository usuarios;

	private Usuario ana;
	private Sector sector;
	private Sector otroSector;
	private Cultivo cultivo;

	@BeforeEach
	void prepararDatos() {
		tareas.deleteAll();
		reportes.deleteAll();
		cultivos.deleteAll();
		sectores.deleteAll();
		usuarios.deleteAll();
		ana = crearUsuario("ana@huerta.cl", Rol.COLABORADOR);
		crearUsuario("admin@huerta.cl", Rol.ADMIN);
		sector = crearSector("S-1");
		otroSector = crearSector("S-2");
		cultivo = new Cultivo();
		cultivo.setNombre("Tomate");
		cultivo.setSector(sector);
		cultivo.setFrecuenciaRiegoDias(2);
		cultivo.setEstado(EstadoCultivo.SEMBRADO);
		cultivo = cultivos.save(cultivo);
	}

	@Test
	@WithMockUser(username = "ana@huerta.cl", roles = "COLABORADOR")
	void permiteSectorNuloYDerivaSectorDelCultivoSinNpe() throws Exception {
		mockMvc.perform(post("/api/reportes-problemas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sectorId":null,"cultivoId":null,"tipo":"Plaga","descripcion":"Hojas dañadas"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sectorId").value(nullValue()));

		mockMvc.perform(post("/api/reportes-problemas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"cultivoId":%d,"tipo":"Plaga","descripcion":"Hojas dañadas"}
								""".formatted(cultivo.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sectorId", is(sector.getId().intValue())));

		mockMvc.perform(post("/api/reportes-problemas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sectorId":%d,"cultivoId":%d,"tipo":"Plaga","descripcion":"Hojas dañadas"}
								""".formatted(otroSector.getId(), cultivo.getId())))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void actualizacionDerivaSectorCuandoSeEnviaSoloCultivo() throws Exception {
		ReporteProblema reporte = new ReporteProblema();
		reporte.setCultivo(cultivo);
		reporte.setSector(sector);
		reporte.setTipo("Plaga");
		reporte.setDescripcion("Hojas dañadas");
		reporte.setFechaReporte(LocalDate.now());
		reporte.setUsuarioReportador(ana);
		reporte.setEstado(EstadoReporteProblema.INFORMADO);
		reporte = reportes.save(reporte);
		String json = """
				{"sectorId":null,"cultivoId":%d,"tipo":"Plaga","descripcion":"Hojas dañadas",
				"fechaReporte":"2026-11-16","usuarioReportadorId":%d,"estado":"EN_REVISION"}
				""".formatted(cultivo.getId(), ana.getId());

		mockMvc.perform(put("/api/reportes-problemas/{id}", reporte.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sectorId", is(sector.getId().intValue())));
	}

	private Usuario crearUsuario(String email, Rol rol) {
		Usuario usuario = new Usuario();
		usuario.setNombre(email);
		usuario.setEmail(email);
		usuario.setPassword("hash");
		usuario.setRol(rol);
		usuario.setTallerGrupo("Taller");
		return usuarios.save(usuario);
	}

	private Sector crearSector(String codigo) {
		Sector sector = new Sector();
		sector.setCodigo(codigo);
		sector.setNombre(codigo);
		sector.setEstado(EstadoSector.DISPONIBLE);
		return sectores.save(sector);
	}
}
