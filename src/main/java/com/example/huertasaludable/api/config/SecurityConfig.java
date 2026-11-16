package com.example.huertasaludable.api.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
		byte[] key = secret.getBytes(StandardCharsets.UTF_8);
		if (key.length < 32) {
			throw new IllegalStateException("JWT_SECRET debe contener al menos 32 bytes");
		}
		return new SecretKeySpec(key, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSecretKey));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
		return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	Duration jwtExpiration(@Value("${app.jwt.expiration}") Duration expiration) {
		if (expiration.isZero() || expiration.isNegative()) {
			throw new IllegalStateException("JWT_EXPIRATION debe ser una duración positiva");
		}
		return expiration;
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> List.of(
				new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role"))));
		return converter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
						.requestMatchers("/api/usuarios/me", "/api/usuarios/me/**").authenticated()
						.requestMatchers("/api/usuarios/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/sectores/**", "/api/cultivos/**").authenticated()
						.requestMatchers("/api/sectores/**", "/api/cultivos/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/tareas/**", "/api/reportes-problemas/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/reportes-problemas").authenticated()
						.requestMatchers(HttpMethod.PATCH, "/api/tareas/*/avance").authenticated()
						.requestMatchers("/api/tareas/**", "/api/reportes-problemas/**").hasRole("ADMIN")
						.anyRequest().denyAll())
				.oauth2ResourceServer(resource -> resource.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
