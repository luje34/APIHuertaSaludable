package com.example.huertasaludable.api.reporte;

import java.time.LocalDate;

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
@Table(name = "reportes_problema")
@Getter
@Setter
@NoArgsConstructor
public class ReporteProblema {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sector_id")
	private Sector sector;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cultivo_id")
	private Cultivo cultivo;

	@Column(nullable = false, length = 100)
	private String tipo;

	@Column(nullable = false, length = 1600)
	private String descripcion;

	@Column(nullable = false)
	private LocalDate fechaReporte;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_reportador_id", nullable = false)
	private Usuario usuarioReportador;

	@Column(length = 2048)
	private String fotografiaUri;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private EstadoReporteProblema estado;

	@Column(length = 1500)
	private String comentarioDocente;

	private LocalDate fechaAtencion;
}
