
package com.samavya.backend.service;

import com.samavya.backend.ServiceListing;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServiceListingService {

    public List<ServiceListing> getAllServices() {
        return List.of(
                new ServiceListing(
                        1L, "Ravi", "Plumbing",
                        new BigDecimal("499.00"), true),
                new ServiceListing(
                        2L, "Anita", "Electrical Repair",
                        new BigDecimal("299.00"), true),
                new ServiceListing(
                        3L, "Kiran", "House Cleaning",
                        new BigDecimal("799.00"), false));
    }
}
