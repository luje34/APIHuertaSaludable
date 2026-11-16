package com.example.huertasaludable.api.config;

import java.util.Locale;

import com.example.huertasaludable.api.usuario.Rol;
import com.example.huertasaludable.api.usuario.Usuario;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder passwordEncoder;
	private final String nombre;
	private final String email;
	private final String password;

	public AdminBootstrap(
			UsuarioRepository usuarios,
			PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap-admin.name}") String nombre,
			@Value("${app.bootstrap-admin.email}") String email,
			@Value("${app.bootstrap-admin.password}") String password) {
		this.usuarios = usuarios;
		this.passwordEncoder = passwordEncoder;
		this.nombre = nombre;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		boolean configurado = !nombre.isBlank() || !email.isBlank() || !password.isBlank();
		if (!configurado) {
			return;
		}
		if (nombre.isBlank() || email.isBlank() || password.isBlank() || password.length() < 10 || password.length() > 72) {
			throw new IllegalStateException(
					"Configure BOOTSTRAP_ADMIN_NAME, BOOTSTRAP_ADMIN_EMAIL y BOOTSTRAP_ADMIN_PASSWORD (10-72 caracteres)");
		}
		if (usuarios.existsByRol(Rol.ADMIN)) {
			return;
		}
		String normalizado = email.trim().toLowerCase(Locale.ROOT);
		if (usuarios.existsByEmailIgnoreCase(normalizado)) {
			throw new IllegalStateException("El correo configurado para el administrador inicial ya está en uso");
		}
		Usuario admin = new Usuario();
		admin.setNombre(nombre.trim());
		admin.setEmail(normalizado);
		admin.setPassword(passwordEncoder.encode(password));
		admin.setRol(Rol.ADMIN);
		admin.setTallerGrupo("Administración");
		admin.setActivo(true);
		usuarios.save(admin);
	}
}
