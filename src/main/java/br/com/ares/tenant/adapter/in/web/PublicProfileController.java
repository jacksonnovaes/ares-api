package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.PublicProfileUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import br.com.ares.tenant.domain.model.PublicServiceSource;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class PublicProfileController {

    private final PublicProfileUseCase profiles;

    public PublicProfileController(PublicProfileUseCase profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/api/v1/public-profile-settings")
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    PublicProfileUseCase.ProfileSettings settings() {
        return profiles.getSettings();
    }

    @PutMapping("/api/v1/public-profile-settings")
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    PublicProfileUseCase.ProfileSettings update(@Valid @RequestBody UpdateProfileRequest request) {
        return profiles.update(new PublicProfileUseCase.UpdateProfileCommand(request.enabled(), request.headline(),
                request.description(), request.whatsapp(), request.email(), request.city(), request.serviceArea(),
                request.showPrices(), request.serviceSource(), request.manualServices() == null ? List.of()
                        : request.manualServices().stream().map(value -> new PublicProfileUseCase.ManualService(
                                value.name(), value.description(), value.basePrice())).toList(),
                request.accentColor(), request.backgroundColor(), request.textColor(), request.showLogo(),
                request.backgroundOverlayPercentage()));
    }

    @GetMapping("/api/v1/public/profiles/{slug}")
    PublicProfileUseCase.PublicProfile published(
            @PathVariable @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 80) String slug) {
        return profiles.findPublished(slug);
    }

    record UpdateProfileRequest(@NotNull Boolean enabled,
                                @Size(max = 180) String headline,
                                @Size(max = 1200) String description,
                                @Pattern(regexp = "^$|[+\\d()\\- .]{10,25}", message = "Informe um WhatsApp válido.")
                                String whatsapp,
                                @Email @Size(max = 254) String email,
                                @Size(max = 120) String city,
                                @Size(max = 180) String serviceArea,
                                @NotNull Boolean showPrices,
                                @NotNull PublicServiceSource serviceSource,
                                @Size(max = 24) List<@Valid ManualServiceRequest> manualServices,
                                @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String accentColor,
                                @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String backgroundColor,
                                @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String textColor,
                                @NotNull Boolean showLogo,
                                @Min(0) @Max(90) int backgroundOverlayPercentage) {
    }

    record ManualServiceRequest(@NotBlank @Size(max = 160) String name,
                                @Size(max = 1000) String description,
                                @DecimalMin("0.00") BigDecimal basePrice) {
    }
}
