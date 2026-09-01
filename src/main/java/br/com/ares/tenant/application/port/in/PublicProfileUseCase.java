package br.com.ares.tenant.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import br.com.ares.tenant.domain.model.PublicServiceSource;

public interface PublicProfileUseCase {

    ProfileSettings getSettings();

    ProfileSettings update(UpdateProfileCommand command);

    PublicProfile findPublished(String slug);

    record ProfileSettings(boolean enabled, String slug, String tradeName, String logoUrl, String primaryColor,
                           String headline, String description, String whatsapp, String email, String city,
                           String serviceArea, boolean showPrices, PublicServiceSource serviceSource,
                           List<ManualService> manualServices, String accentColor, String backgroundColor,
                           String textColor, String logoPath, String backgroundImagePath, boolean showLogo,
                           int backgroundOverlayPercentage) {
    }

    record UpdateProfileCommand(boolean enabled, String headline, String description, String whatsapp, String email,
                                String city, String serviceArea, boolean showPrices,
                                PublicServiceSource serviceSource, List<ManualService> manualServices,
                                String accentColor, String backgroundColor, String textColor,
                                boolean showLogo, int backgroundOverlayPercentage) {
    }

    record PublicProfile(String slug, String tradeName, String logoUrl, String primaryColor, String headline,
                         String description, String whatsapp, String email, String city, String serviceArea,
                         boolean showPrices, PublicServiceSource serviceSource, String accentColor,
                         String backgroundColor, String textColor, String logoPath, String backgroundImagePath,
                         boolean showLogo, int backgroundOverlayPercentage, List<PublicService> services) {
    }

    record ManualService(String name, String description, BigDecimal basePrice) {
    }

    record PublicService(String name, String description, BigDecimal basePrice, Integer estimatedMinutes) {
    }
}
