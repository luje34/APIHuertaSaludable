package com.example.huertasaludable.api.sector;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sectores")
@Getter
@Setter
@NoArgsConstructor
public class Sector {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String codigo;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(length = 500)
	private String descripcion;

	@Column(length = 180)
	private String ubicacion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EstadoSector estado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "responsable_id")
	private Usuario responsable;

	@Column(nullable = false)
	private LocalDateTime fechaCreacion;

	@PrePersist
	void alCrear() {
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
		}
	}
}
