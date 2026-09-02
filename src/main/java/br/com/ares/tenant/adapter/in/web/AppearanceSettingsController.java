package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.AppearanceSettingsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appearance-settings")
public class AppearanceSettingsController {

    private final AppearanceSettingsUseCase settings;

    public AppearanceSettingsController(AppearanceSettingsUseCase settings) {
        this.settings = settings;
    }

    @GetMapping
    AppearanceSettingsUseCase.AppearanceSettings get() {
        return settings.get();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    AppearanceSettingsUseCase.AppearanceSettings update(@Valid @RequestBody UpdateAppearanceRequest request) {
        return settings.update(new AppearanceSettingsUseCase.UpdateAppearanceCommand(request.tradeName(),
                request.primaryColor(), request.secondaryColor(), request.borderRadius()));
    }

    record UpdateAppearanceRequest(@NotBlank @Size(min = 2, max = 160) String tradeName,
                                   @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String primaryColor,
                                   @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String secondaryColor,
                                   @Min(6) @Max(24) int borderRadius) {
    }
}

