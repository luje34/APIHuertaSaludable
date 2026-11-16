package com.example.huertasaludable.api.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import com.example.huertasaludable.api.usuario.EmailDuplicadoException;
import com.example.huertasaludable.api.usuario.Rol;
import com.example.huertasaludable.api.usuario.Usuario;
import com.example.huertasaludable.api.usuario.UsuarioRepository;
import com.example.huertasaludable.api.usuario.dto.UsuarioResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final Duration expiration;

	public AuthService(
			UsuarioRepository usuarios,
			PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder,
			Duration jwtExpiration) {
		this.usuarios = usuarios;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.expiration = jwtExpiration;
	}

	@Transactional
	public AuthResponse registrar(RegistroRequest request) {
		String email = normalizarEmail(request.email());
		if (usuarios.existsByEmailIgnoreCase(email)) {
			throw new EmailDuplicadoException();
		}
		Usuario usuario = new Usuario();
		usuario.setNombre(request.nombre().trim());
		usuario.setEmail(email);
		usuario.setPassword(passwordEncoder.encode(request.password()));
		usuario.setRol(Rol.COLABORADOR);
		usuario.setTallerGrupo(request.tallerGrupo().trim());
		usuario.setActivo(true);
		return emitirToken(usuarios.save(usuario));
	}

	@Transactional(readOnly = true)
	public AuthResponse iniciarSesion(LoginRequest request) {
		Usuario usuario = usuarios.findByEmailIgnoreCase(normalizarEmail(request.email()))
				.filter(Usuario::isActivo)
				.filter(encontrado -> passwordEncoder.matches(request.password(), encontrado.getPassword()))
				.orElseThrow(() -> new BadCredentialsException("Correo o contraseña inválidos"));
		return emitirToken(usuario);
	}

	private AuthResponse emitirToken(Usuario usuario) {
		Instant ahora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("huerta-saludable-api")
				.issuedAt(ahora)
				.expiresAt(ahora.plus(expiration))
				.subject(usuario.getEmail())
				.claim("uid", usuario.getId())
				.claim("role", usuario.getRol().name())
				.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
		return new AuthResponse(token, "Bearer", expiration.toSeconds(), UsuarioResponse.from(usuario));
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
