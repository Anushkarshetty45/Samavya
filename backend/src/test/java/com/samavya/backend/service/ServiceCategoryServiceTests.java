package com.samavya.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samavya.backend.ServiceCategory;
import com.samavya.backend.repository.ServiceCategoryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

class ServiceCategoryServiceTests {

    private ServiceCategoryRepository repository;
    private ServiceCategoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(ServiceCategoryRepository.class);
        service = new ServiceCategoryService(repository);
    }

    @Test
    void returnsRepositoryResultsWithoutHidingDatabaseFailures() {
        DataAccessResourceFailureException failure =
                new DataAccessResourceFailureException("Database unavailable");
        when(repository.findAllActive()).thenThrow(failure);

        assertThatThrownBy(service::getActiveCategories)
                .isSameAs(failure);
    }

    @Test
    void returnsTheActiveCategoriesProvidedByTheRepository() {
        List<ServiceCategory> categories =
                List.of(new ServiceCategory(1, "Plumber", "Repairs"));
        when(repository.findAllActive()).thenReturn(categories);

        assertThat(service.getActiveCategories()).isEqualTo(categories);
    }
}
