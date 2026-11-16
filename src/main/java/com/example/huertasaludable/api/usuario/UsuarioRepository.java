package com.example.huertasaludable.api.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	java.util.Optional<Usuario> findByEmailIgnoreCase(String email);

	boolean existsByRol(Rol rol);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
