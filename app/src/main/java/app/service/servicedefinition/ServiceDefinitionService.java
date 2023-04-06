package app.service.servicedefinition;

import app.dto.servicedefinition.ServiceDefinitionDTO;
import app.model.servicedefinition.ServiceDefinition;
import app.model.servicedefinition.ServiceDefinitionRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class ServiceDefinitionService {

    private final ServiceDefinitionRepository serviceDefinitionRepository;
    public ServiceDefinitionService(ServiceDefinitionRepository serviceDefinitionRepository) {
        this.serviceDefinitionRepository = serviceDefinitionRepository;
    }
    public ServiceDefinitionDTO getServiceDefinition(String serviceCode) {
        Optional<ServiceDefinition> byId = serviceDefinitionRepository.findById(serviceCode);
        return byId.map(ServiceDefinitionDTO::new).orElse(null);
    }
}
