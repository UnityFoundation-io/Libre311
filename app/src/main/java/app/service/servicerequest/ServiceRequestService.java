package app.service.servicerequest;

import app.dto.servicerequest.GetServiceRequestsDTO;
import app.dto.servicerequest.PostRequestServiceRequestDTO;
import app.dto.servicerequest.PostResponseServiceRequestDTO;
import app.dto.servicerequest.ServiceRequestDTO;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Singleton
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRepository serviceRepository;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository, ServiceRepository serviceRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRepository = serviceRepository;
    }

    public PostResponseServiceRequestDTO createServiceRequest(PostRequestServiceRequestDTO serviceRequestDTO) {

        Optional<Service> serviceByServiceCodeOptional = serviceRepository.findByServiceCode(serviceRequestDTO.getServiceCode());

        if (serviceByServiceCodeOptional.isEmpty()) {
            return null; // todo return 'corresponding service not found
        }

        // validate if a location is provided
        boolean latLongProvided = StringUtils.hasText(serviceRequestDTO.getLatitude()) &&
                StringUtils.hasText(serviceRequestDTO.getLongitude());

        if (!latLongProvided &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressString()) &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressId())) {
            return null; // todo throw exception
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


        return new PostResponseServiceRequestDTO(serviceRequestRepository.save(transformDtoToServiceRequest(serviceRequestDTO, serviceByServiceCodeOptional.get())));
    }

    private ServiceRequest transformDtoToServiceRequest(PostRequestServiceRequestDTO serviceRequestDTO, Service service) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService(service);
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

    public Page<ServiceRequestDTO> findAll(GetServiceRequestsDTO requestDTO) {
        return getServiceRequestPage(requestDTO).map(ServiceRequestDTO::new);
    }

    private Page<ServiceRequest> getServiceRequestPage(GetServiceRequestsDTO requestDTO) {
        String serviceRequestIds = requestDTO.getId();
        String serviceCode = requestDTO.getServiceCode();
        ServiceRequestStatus status = requestDTO.getStatus();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();
        Pageable pageable = requestDTO.getPageable();

        if (StringUtils.hasText(serviceRequestIds)) {
            List<String> requestIds = Arrays.stream(serviceRequestIds.split(",")).map(String::trim).collect(Collectors.toList());
            return serviceRequestRepository.findByIdIn(requestIds, pageable);
        }


        if (StringUtils.hasText(serviceCode) && status != null) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedBetween(serviceCode, status, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedAfter(serviceCode, status, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedBefore(serviceCode, status, endDate, pageable);
            }

            return serviceRequestRepository.findByServiceServiceCodeAndStatus(serviceCode, status, pageable);
        } else if (StringUtils.hasText(serviceCode) && status == null) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedBetween(serviceCode, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedAfter(serviceCode, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedBefore(serviceCode, endDate, pageable);
            }

            return serviceRequestRepository.findByServiceServiceCode(serviceCode, pageable);
        } else if (status != null && StringUtils.isEmpty(serviceCode)) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByStatusAndDateCreatedBetween(status, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByStatusAndDateCreatedAfter(status, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByStatusAndDateCreatedBefore(status, endDate, pageable);
            }

            return serviceRequestRepository.findByStatus(status, pageable);
        }


        if (startDate != null && endDate != null) {
            return serviceRequestRepository.findByDateCreatedBetween(startDate, endDate, pageable);
        } else if (startDate != null && endDate == null) {
            // just start
            return serviceRequestRepository.findByDateCreatedAfter(startDate, pageable);
        } else if (startDate == null && endDate != null) {
            // just end
            return serviceRequestRepository.findByDateCreatedBefore(endDate, pageable);
        }

        return serviceRequestRepository.findAll(pageable);
    }

    public ServiceRequestDTO getServiceRequest(String serviceRequestId) {
        Optional<ServiceRequest> byId = serviceRequestRepository.findById(serviceRequestId);
        return byId.map(ServiceRequestDTO::new).orElse(null);
    }
}
