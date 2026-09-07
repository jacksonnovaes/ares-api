package br.com.ares.asset.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAssetTypeRequest(
        @NotBlank @Size(max = 100) String name
) {
}
