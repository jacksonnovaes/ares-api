package br.com.ares.identity.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String refreshToken) {
}