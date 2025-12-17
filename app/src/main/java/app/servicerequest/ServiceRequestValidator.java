package app.servicerequest;

import app.servicedefinition.ServiceDefinitionAttributeDTO;
import app.exception.Libre311BaseException;
import app.jurisdiction.JurisdictionBoundaryService;
import app.servicedefinition.ServiceDefinition;
import app.servicedefinition.ServiceRepository;
import app.servicedefinition.ServiceDefinitionAttribute;
import app.storage.StorageUrlUtil;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ServiceRequestValidator {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestValidator.class);

    static class ServiceRequestOutOfBoundsException extends Libre311BaseException {
        public ServiceRequestOutOfBoundsException() {
            super("The service request is out of bounds", HttpStatus.BAD_REQUEST);
        }
    }
    private final StorageUrlUtil storageUrlUtil;
    private final ServiceRepository serviceRepository;
    private final JurisdictionBoundaryService jurisdictionBoundaryService;

    public ServiceRequestValidator(StorageUrlUtil storageUrlUtil, ServiceRepository serviceRepository, JurisdictionBoundaryService jurisdictionBoundaryService) {
        this.storageUrlUtil = storageUrlUtil;
        this.serviceRepository = serviceRepository;
        this.jurisdictionBoundaryService = jurisdictionBoundaryService;
    }

    void validateRequestInBoundaries(ServiceRequestPostRequest serviceRequestDTO, String jurisdictionId){
        double lat = Double.parseDouble(serviceRequestDTO.getLatitude());
        double lng = Double.parseDouble(serviceRequestDTO.getLongitude());
        if (!jurisdictionBoundaryService.existsInJurisdiction(jurisdictionId, lat, lng)){
            throw new ServiceRequestOutOfBoundsException();
        }
    }

    void requiredAttributesPresent(List<ServiceDefinitionAttribute> serviceDefinitionAttributes, List<ServiceDefinitionAttributeDTO> requestAttributes) {
        // collect all required attributes
        List<Long> requiredCodes = serviceDefinitionAttributes.stream()
                .filter(ServiceDefinitionAttribute::isRequired)
                .map(ServiceDefinitionAttribute::getId)
                .collect(Collectors.toList());

        // for each attr, check if it exists in requestAttributes
        List<Long> requestCodes = requestAttributes.stream()
                .map(ServiceDefinitionAttributeDTO::getId)
                .collect(Collectors.toList());
        LOG.debug("Required attributes: {}", requiredCodes);
        LOG.debug("Request attributes: {}", requestCodes);
        if (!requestCodes.containsAll(requiredCodes)){
            throw new ServiceRequestService.InvalidServiceRequestException("Submitted Service Request does not contain required attribute values.");

        }
    }

    void validMediaUrl(String mediaUrl) {

        if (mediaUrl != null && !mediaUrl.startsWith(storageUrlUtil.getBucketUrlString())){
            throw new ServiceRequestService.InvalidServiceRequestException("Media URL is invalid.");
        }
    }

    ServiceDefinition validService(ServiceRequestPostRequest serviceRequestDTO, String jurisdictionId){
        Optional<ServiceDefinition> serviceByServiceCodeOptional = serviceRepository.findById(serviceRequestDTO.getServiceCode());

        if (serviceByServiceCodeOptional.isEmpty()) {
            throw new ServiceRequestService.InvalidServiceRequestException("Corresponding service is not found.");
        }

        if (!jurisdictionId.equals(serviceByServiceCodeOptional.get().getJurisdiction().getId())) {
            throw new ServiceRequestService.InvalidServiceRequestException(
                    "Mismatch between jurisdiction_id provided and Service's associated jurisdiction.");
        }
        return serviceByServiceCodeOptional.get();
    }

}
