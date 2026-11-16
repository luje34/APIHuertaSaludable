package com.example.huertasaludable.api.auth;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.huertasaludable.api.cultivo.CultivoRepository;
import com.example.huertasaludable.api.reporte.ReporteProblemaRepository;
import com.example.huertasaludable.api.sector.SectorRepository;
import com.example.huertasaludable.api.tarea.TareaRepository;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UsuarioRepository usuarios;
	@Autowired
	private TareaRepository tareas;
	@Autowired
	private ReporteProblemaRepository reportes;
	@Autowired
	private CultivoRepository cultivos;
	@Autowired
	private SectorRepository sectores;
	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void limpiarDatos() {
		tareas.deleteAll();
		reportes.deleteAll();
		cultivos.deleteAll();
		sectores.deleteAll();
		usuarios.deleteAll();
	}

	@Test
	void registroSiempreEsColaboradorYTokenSoloPermiteSuPerfil() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Ana","email":"ANA@HUERTA.CL","password":"clave-segura-123",
								"tallerGrupo":"Taller 5A","rol":"ADMIN","activo":false}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.usuario.rol", is("COLABORADOR")))
				.andExpect(jsonPath("$.usuario.password").doesNotExist())
				.andReturn();
		String token = objectMapper.readTree(result.getResponse().getContentAsString())
				.path("accessToken").asText();
		org.junit.jupiter.api.Assertions.assertTrue(
				usuarios.findByEmailIgnoreCase("ana@huerta.cl").orElseThrow().getPassword().startsWith("$2"));

		mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email", is("ana@huerta.cl")));
		mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/api/usuarios")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Admin falso","email":"evil@huerta.cl","password":"clave-segura",
								"rol":"ADMIN","tallerGrupo":"Admin","activo":true}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void loginValidaCredencialesYRegistroRechazaClavesDebiles() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Ana","email":"ana@huerta.cl","password":"corta","tallerGrupo":"Taller"}
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Ana","email":"ana@huerta.cl","password":"clave-segura-123","tallerGrupo":"Taller"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"ana@huerta.cl","password":"clave-segura-123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType", is("Bearer")))
				.andExpect(jsonPath("$.expiresIn").isNumber());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"ana@huerta.cl","password":"incorrecta"}
								"""))
				.andExpect(status().isUnauthorized());
	}
}
