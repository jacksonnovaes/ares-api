package br.com.ares.tenant.adapter.in.web;

import br.com.ares.tenant.application.port.in.PrivacyUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/privacy")
@PreAuthorize("hasRole('ADMIN')")
public class PrivacyController {

    private final PrivacyUseCase privacy;

    public PrivacyController(PrivacyUseCase privacy) {
        this.privacy = privacy;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PrivacyUseCase.TenantDataExport> exportData() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ares-export.json")
                .body(privacy.exportData());
    }

    @DeleteMapping("/account")
    PrivacyUseCase.DataDeletionResult deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        return privacy.deleteAccount(new PrivacyUseCase.DeleteAccountCommand(
                request.currentPassword(), request.confirmation()));
    }

    record DeleteAccountRequest(@NotBlank String currentPassword, @NotBlank String confirmation) {
    }
}
