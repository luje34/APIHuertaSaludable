package com.example.huertasaludable.api.tarea;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.huertasaludable.api.cultivo.Cultivo;
import com.example.huertasaludable.api.sector.Sector;
import com.example.huertasaludable.api.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tareas")
@Getter
@Setter
@NoArgsConstructor
public class Tarea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String tipo;

	@Column(nullable = false, length = 1200)
	private String descripcion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sector_id")
	private Sector sector;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cultivo_id")
	private Cultivo cultivo;

	@Column(nullable = false)
	private LocalDate fechaProgramada;

	private LocalDate fechaRealizacion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estudiante_id", nullable = false)
	private Usuario estudiante;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "docente_creador_id", nullable = false)
	private Usuario docenteCreador;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EstadoTarea estado;

	@Column(length = 1500)
	private String comentarioEstudiante;

	@Column(length = 1500)
	private String comentarioDocente;

	@Column(length = 2048)
	private String fotografiaEvidenciaUri;

	private LocalDateTime fechaValidacion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "docente_validador_id")
	private Usuario docenteValidador;
}
