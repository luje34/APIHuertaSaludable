package com.example.huertasaludable.api.cultivo;

import java.time.LocalDate;

import com.example.huertasaludable.api.sector.Sector;
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
@Table(name = "cultivos")
@Getter
@Setter
@NoArgsConstructor
public class Cultivo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Column(length = 1000)
	private String descripcion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sector_id", nullable = false)
	private Sector sector;

	private LocalDate fechaSiembra;
	private LocalDate fechaTrasplante;
	private LocalDate fechaEstimadaCosecha;

	@Column(nullable = false)
	private Integer frecuenciaRiegoDias;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EstadoCultivo estado;

	@Column(length = 1500)
	private String observaciones;

	@Column(length = 2048)
	private String fotografiaUri;
}
