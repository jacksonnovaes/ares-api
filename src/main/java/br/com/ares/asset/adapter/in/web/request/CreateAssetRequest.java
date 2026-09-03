package br.com.ares.asset.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record CreateAssetRequest(@NotNull UUID customerId,
                                   @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{1,50}") String type,
                                   @NotBlank @Size(max = 160) String name, @Size(max = 100) String brand,
                                   @Size(max = 100) String model, @Size(max = 120) String serialNumber,
                                   Map<String, String> attributes) {
}
