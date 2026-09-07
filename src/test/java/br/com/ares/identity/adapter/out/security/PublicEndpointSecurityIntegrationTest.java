package br.com.ares.identity.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicEndpointSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void registrationIgnoresAnInvalidBearerTokenBecauseItIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/register")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whatsappPlanSimulationIsPublicAndDoesNotSendARealMessage() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/plan-whatsapp-simulation")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tradeName":"Oficina Ares","whatsapp":"11999999999","plan":"PRO","billingCycle":"MONTHLY","additionalUserSeats":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryMode").value("SIMULATION"))
                .andExpect(jsonPath("$.plan").value("PRO"))
                .andExpect(jsonPath("$.originalPrice").value(69.90))
                .andExpect(jsonPath("$.price").value(69.90))
                .andExpect(jsonPath("$.userLimit").value(3));
    }

    @Test
    void registrationFeatureConfigurationIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/registration-config")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionPaymentSimulationEnabled").value(true))
                .andExpect(jsonPath("$.couponEnabled").value(true))
                .andExpect(jsonPath("$.termsVersion").value("2026-08-27"))
                .andExpect(jsonPath("$.plans[0].code").value("SOLO"))
                .andExpect(jsonPath("$.plans[0].monthlyPrice").value(29.90))
                .andExpect(jsonPath("$.plans[0].features[1]").value("Agenda de atendimentos"))
                .andExpect(jsonPath("$.plans[2].includedUsers").value(10));
    }

    @Test
    void couponValidationIsPublicAndCalculatesPriceOnTheServer() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/coupon-validation")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"plan":"PRO","billingCycle":"MONTHLY","additionalUserSeats":0,"couponCode":"bemvindo20"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("BEMVINDO20"))
                .andExpect(jsonPath("$.discountPercentage").value(20))
                .andExpect(jsonPath("$.price").value(55.92));
    }

    @Test
    void professionalProfileIsPublicAndIgnoresAnInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/public/profiles/perfil-inexistente")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("public_profile_not_found"));
    }

    @Test
    void professionalMediaIsPublicButUploadManagementRemainsProtected() throws Exception {
        mockMvc.perform(get("/api/v1/public/media/00000000-0000-0000-0000-000000000000/"
                        + "logo-00000000-0000-0000-0000-000000000000.png")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("public_profile_media_not_found"));

        mockMvc.perform(delete("/api/v1/public-profile-media/LOGO"))
                .andExpect(status().isUnauthorized());
    }
}
