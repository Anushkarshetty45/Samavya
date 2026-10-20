package com.samavya.backend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.samavya.backend.service.MarketplaceService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;

class MarketplaceControllerTests {

    private MarketplaceService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(MarketplaceService.class);
        mockMvc = standaloneSetup(new MarketplaceController(service))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory) {
                        return Jwt.withTokenValue("test-token")
                                .header("alg", "none")
                                .subject("7")
                                .build();
                    }
                })
                .build();
    }

    @Test
    void returnsPublicProviderProfileFieldsOnly() throws Exception {
        when(service.getProviderProfile(5)).thenReturn(Optional.of(
                new ProviderProfile(5, "Ravi", "Plumbing", true, "VERIFIED")));

        mockMvc.perform(get("/api/provider-profiles/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.providerName").value("Ravi"))
                .andExpect(jsonPath("$.description").value("Plumbing"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test
    void rejectsInvalidOrMissingProviderIds() throws Exception {
        mockMvc.perform(get("/api/provider-profiles/0"))
                .andExpect(status().isBadRequest());
        when(service.getProviderProfile(99)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/provider-profiles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void forwardsOptionalCommunityAndCategoryFilters() throws Exception {
        ProviderServiceListing listing = new ProviderServiceListing(
                3,
                5,
                "Ravi",
                "Plumbing",
                true,
                "VERIFIED",
                8,
                9,
                "Plumber",
                "Pipes and leaks",
                new BigDecimal("100.00"),
                List.of(new GigSummary(4, "Tap repair", null, "FIXED",
                        new BigDecimal("150.00"), "ACTIVE")));
        when(service.discoverProviderServices(7L, 8L, 9L)).thenReturn(List.of(listing));

        mockMvc.perform(get("/api/service-discovery")
                        .param("communityId", "8")
                        .param("categoryId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].communityId").value(8))
                .andExpect(jsonPath("$[0].categoryName").value("Plumber"))
                .andExpect(jsonPath("$[0].gigs[0].pricingType").value("FIXED"))
                .andExpect(jsonPath("$[0].gigs[0].price").value(150.0));
        verify(service).discoverProviderServices(7L, 8L, 9L);
    }

    @Test
    void rejectsNonPositiveDiscoveryFilters() throws Exception {
        mockMvc.perform(get("/api/service-discovery").param("communityId", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/service-discovery").param("categoryId", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void doesNotExposeUnauthenticatedWriteEndpoints() throws Exception {
        mockMvc.perform(post("/api/provider-profiles/5"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/service-discovery"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/provider-services"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/gigs"))
                .andExpect(status().isNotFound());
    }
}
