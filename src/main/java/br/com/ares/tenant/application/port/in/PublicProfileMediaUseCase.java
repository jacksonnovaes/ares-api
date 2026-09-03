package br.com.ares.tenant.application.port.in;

import br.com.ares.tenant.domain.model.PublicProfileMediaKind;

public interface PublicProfileMediaUseCase {

    StoredMedia store(PublicProfileMediaKind kind, String originalFilename, String declaredContentType,
                      byte[] content);

    void remove(PublicProfileMediaKind kind);

    MediaContent load(String tenantDirectory, String filename);

    void deleteTenantFiles(java.util.UUID tenantId);

    record StoredMedia(PublicProfileMediaKind kind, String path) {
    }

    record MediaContent(byte[] bytes, String contentType) {
    }
}
