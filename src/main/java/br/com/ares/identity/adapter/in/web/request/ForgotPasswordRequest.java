package br.com.ares.identity.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public    record ForgotPasswordRequest(@NotBlank @Email String email) {
}