package app.service.service;

import app.dto.service.ServiceDTO;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public Page<ServiceDTO> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable).map(ServiceDTO::new);
    }

    public String getServiceDefinition(String serviceCode) {
        Optional<Service> byServiceCode = serviceRepository.findByServiceCode(serviceCode);

        if (byServiceCode.isEmpty()) {
            return null;
        } else if (byServiceCode.get().getServiceDefinitionJson() == null) {
            return null;
        }

        return byServiceCode.get().getServiceDefinitionJson();
    }
}
