package com.samavya.backend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.samavya.backend.service.ServiceCategoryService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

class ServiceCategoryControllerTests {

    private ServiceCategoryService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ServiceCategoryService.class);
        mockMvc = standaloneSetup(new ServiceCategoryController(service)).build();
    }

    @Test
    void returnsCategoriesAsJson() throws Exception {
        when(service.getActiveCategories())
                .thenReturn(List.of(new ServiceCategory(7, "Plumber", "Pipes and leaks")));

        mockMvc.perform(get("/api/service-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Plumber"))
                .andExpect(jsonPath("$[0].description").value("Pipes and leaks"));
    }

    @Test
    void returnsAnActiveCategoryById() throws Exception {
        when(service.getActiveCategory(7))
                .thenReturn(Optional.of(new ServiceCategory(7, "Plumber", "Pipes and leaks")));

        mockMvc.perform(get("/api/service-categories/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Plumber"));
    }

    @Test
    void returnsNotFoundWhenTheCategoryIsMissingOrInactive() throws Exception {
        when(service.getActiveCategory(7)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/service-categories/7"))
                .andExpect(status().isNotFound());
    }
}
