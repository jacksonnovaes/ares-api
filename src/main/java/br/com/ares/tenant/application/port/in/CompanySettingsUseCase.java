package br.com.ares.tenant.application.port.in;

public interface CompanySettingsUseCase {

    CompanySettings get();

    CompanySettings update(UpdateCompanySettingsCommand command);

    record CompanySettings(boolean requireAssets) {
    }

    record UpdateCompanySettingsCommand(boolean requireAssets) {
    }
}
