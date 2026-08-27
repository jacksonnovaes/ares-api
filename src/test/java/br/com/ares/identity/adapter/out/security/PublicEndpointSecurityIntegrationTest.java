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
                                {"tradeName":"Oficina Ares","whatsapp":"11999999999","plan":"PROFESSIONAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryMode").value("SIMULATION"))
                .andExpect(jsonPath("$.plan").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.originalPrice").value(99.90))
                .andExpect(jsonPath("$.monthlyPrice").value(99.90));
    }

    @Test
    void registrationFeatureConfigurationIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/registration-config")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionPaymentSimulationEnabled").value(true))
                .andExpect(jsonPath("$.couponEnabled").value(true))
                .andExpect(jsonPath("$.termsVersion").value("2026-08-27"));
    }

    @Test
    void couponValidationIsPublicAndCalculatesPriceOnTheServer() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/coupon-validation")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-from-an-old-cookie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"plan":"PROFESSIONAL","couponCode":"bemvindo20"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("BEMVINDO20"))
                .andExpect(jsonPath("$.discountPercentage").value(20))
                .andExpect(jsonPath("$.monthlyPrice").value(79.92));
    }
}
