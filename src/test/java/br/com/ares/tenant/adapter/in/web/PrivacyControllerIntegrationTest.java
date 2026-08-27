package br.com.ares.tenant.adapter.in.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PrivacyControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void exportsDataWithoutCredentialsAndDeletesTheWholeTenantAfterConfirmation() throws Exception {
        mockMvc.perform(post("/api/v1/tenants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "legalName":"Privacidade Testes Ltda.",
                                  "tradeName":"Privacidade Testes",
                                  "slug":"privacidade-testes",
                                  "document":"12345678000270",
                                  "primaryColor":"#2457E6",
                                  "plan":"ESSENTIAL",
                                  "whatsapp":"11988887777",
                                  "couponCode":"BEMVINDO20",
                                  "simulatedPaymentApproved":true,
                                  "termsAccepted":true,
                                  "privacyNoticeAcknowledged":true,
                                  "termsVersion":"2026-08-27",
                                  "privacyVersion":"2026-08-27",
                                  "admin":{
                                    "name":"Admin Privacidade",
                                    "email":"privacy-integration@example.com",
                                    "password":"SenhaForte#123",
                                    "passwordConfirmation":"SenhaForte#123"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monthlyPrice").value(39.92));

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"privacy-integration@example.com","password":"SenhaForte#123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode login = objectMapper.readTree(loginBody);
        String accessToken = login.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/privacy/export")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.slug").value("privacidade-testes"))
                .andExpect(jsonPath("$.company.document").value("12345678000270"))
                .andExpect(jsonPath("$.users[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.auditTrail").isArray());

        mockMvc.perform(delete("/api/v1/privacy/account")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"SenhaForte#123","confirmation":"EXCLUIR privacidade-testes"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptId").isNotEmpty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"privacy-integration@example.com","password":"SenhaForte#123"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
