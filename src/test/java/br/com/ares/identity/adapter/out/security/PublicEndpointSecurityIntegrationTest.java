package br.com.ares.identity.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
