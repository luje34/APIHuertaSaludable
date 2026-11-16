package com.example.huertasaludable.api.auth;

import com.example.huertasaludable.api.usuario.dto.UsuarioResponse;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UsuarioResponse usuario) {
}
