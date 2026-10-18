
package com.samavya.backend;

import com.samavya.backend.service.ServiceListingService;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {

    private final ServiceListingService service;

    public HelloController(ServiceListingService service) {
        this.service = service;
    }

    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from Samavya!",
                "app", "Samavya",
                "status", "running");
    }

    @GetMapping("/api/services/sample")
    public ServiceListing sampleService() {
        return new ServiceListing(
                1L,
                "Ravi",
                "Plumbing",
                new BigDecimal("499.00"),
                true);
    }

    @GetMapping("api/services")
    public List<ServiceListing> getServices() {
        return service.getAllServices();

    }
}
