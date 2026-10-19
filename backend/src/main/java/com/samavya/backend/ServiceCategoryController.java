package com.samavya.backend;

import com.samavya.backend.service.ServiceCategoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryController {

    private final ServiceCategoryService service;

    public ServiceCategoryController(ServiceCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceCategory> getActiveCategories() {
        return service.getActiveCategories();
    }

    @GetMapping("/{id}")
    public ServiceCategory getActiveCategory(@PathVariable long id) {
        return service.getActiveCategory(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
