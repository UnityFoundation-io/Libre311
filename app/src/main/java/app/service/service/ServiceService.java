package app.service.service;

import app.dto.service.ServiceDTO;
import app.model.service.ServiceRepository;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceDTO> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable).map(ServiceDTO::new).getContent();
    }
}
