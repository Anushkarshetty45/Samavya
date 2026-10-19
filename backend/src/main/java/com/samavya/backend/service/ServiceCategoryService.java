package com.samavya.backend.service;

import com.samavya.backend.ServiceCategory;
import com.samavya.backend.repository.ServiceCategoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository repository;

    public ServiceCategoryService(ServiceCategoryRepository repository) {
        this.repository = repository;
    }

    public List<ServiceCategory> getActiveCategories() {
        return repository.findAllActive();
    }

    public Optional<ServiceCategory> getActiveCategory(long id) {
        return repository.findActiveById(id);
    }
}
