package br.com.ares.tenant.application.port.in;

public interface AppearanceSettingsUseCase {

    AppearanceSettings get();

    AppearanceSettings update(UpdateAppearanceCommand command);

    record AppearanceSettings(String tradeName, String logoUrl, String primaryColor, String secondaryColor,
                              int borderRadius) {
    }

    record UpdateAppearanceCommand(String tradeName, String primaryColor, String secondaryColor, int borderRadius) {
    }
}

