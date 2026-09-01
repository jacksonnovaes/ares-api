package br.com.ares.tenant.adapter.in.web;

import br.com.ares.shared.domain.BusinessException;
import br.com.ares.tenant.application.port.in.PublicProfileMediaUseCase;
import br.com.ares.tenant.domain.model.PublicProfileMediaKind;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Validated
@RestController
public class PublicProfileMediaController {

    private final PublicProfileMediaUseCase media;

    public PublicProfileMediaController(PublicProfileMediaUseCase media) {
        this.media = media;
    }

    @PostMapping(path = "/api/v1/public-profile-media/{kind}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    PublicProfileMediaUseCase.StoredMedia upload(@PathVariable PublicProfileMediaKind kind,
                                                  @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("public_profile_media_empty", "Selecione uma imagem.");
        }
        return media.store(kind, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    @DeleteMapping("/api/v1/public-profile-media/{kind}")
    @PreAuthorize("hasAuthority('TENANT_CONFIGURE')")
    ResponseEntity<Void> remove(@PathVariable PublicProfileMediaKind kind) {
        media.remove(kind);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/public/media/{tenantDirectory}/{filename}")
    ResponseEntity<byte[]> image(
            @PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}") String tenantDirectory,
            @PathVariable @Pattern(regexp = "(?:logo|background)-[0-9a-fA-F-]{36}\\.(?:png|jpg|webp)")
            String filename) {
        var content = media.load(tenantDirectory, filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, content.contentType())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(content.bytes());
    }
}
