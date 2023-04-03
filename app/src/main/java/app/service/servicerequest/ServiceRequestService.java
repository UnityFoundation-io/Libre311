package app.service.servicerequest;

import app.dto.servicerequest.PostServiceRequestDTO;
import app.dto.servicerequest.ServiceRequestDTO;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

import java.util.Optional;


@Singleton
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRepository serviceRepository;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository, ServiceRepository serviceRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRepository = serviceRepository;
    }

    public ServiceRequestDTO createServiceRequest(PostServiceRequestDTO serviceRequestDTO) {

        // validate if a location is provided
        boolean latLongProvided = StringUtils.hasText(serviceRequestDTO.getLatitude()) &&
                StringUtils.hasText(serviceRequestDTO.getLongitude());

        if (!latLongProvided &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressString()) &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressId())) {
            return null; // throw exception
        }

        // validate if additional attributes are required
        String serviceCode = serviceRequestDTO.getServiceCode();
        Optional<Service> serviceOptional = serviceRepository.findById(serviceCode);
        if (serviceOptional.isEmpty()) {
            return null; // not found
        }
        Service service = serviceOptional.get();
        if (service.isMetadata()) {
            // todo query corresponding service definition and cross-validate if required attributes are provided
        }


        return new ServiceRequestDTO(serviceRequestRepository.save(transformDtoToServiceRequest(serviceRequestDTO)));
    }

    private ServiceRequest transformDtoToServiceRequest(PostServiceRequestDTO serviceRequestDTO) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceCode(serviceRequestDTO.getServiceCode());
        serviceRequest.setLatitude(serviceRequestDTO.getLatitude());
        serviceRequest.setLongitude(serviceRequestDTO.getLongitude());
        serviceRequest.setAddressString(serviceRequestDTO.getAddressString());
        serviceRequest.setAddressId(serviceRequestDTO.getAddressId());
        serviceRequest.setEmail(serviceRequestDTO.getEmail());
        serviceRequest.setDeviceId(serviceRequestDTO.getDeviceId());
        serviceRequest.setAccountId(serviceRequestDTO.getAccountId());
        serviceRequest.setFirstName(serviceRequestDTO.getFirstName());
        serviceRequest.setLastName(serviceRequestDTO.getLastName());
        serviceRequest.setPhone(serviceRequestDTO.getPhone());
        serviceRequest.setDescription(serviceRequestDTO.getDescription());
        serviceRequest.setMediaUrl(serviceRequestDTO.getMediaUrl());
        return serviceRequest;
    }
}
