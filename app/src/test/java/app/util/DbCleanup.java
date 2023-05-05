package app.util;

import app.model.servicerequest.ServiceRequestRepository;
import jakarta.inject.Singleton;

import javax.transaction.Transactional;

@Singleton
public class DbCleanup {

    private final ServiceRequestRepository serviceRequestRepository;

    public DbCleanup(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }


    @Transactional
    public void cleanup() {
        serviceRequestRepository.deleteAll();
    }
}