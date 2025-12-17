package app.servicerequest;

import app.jurisdiction.Jurisdiction;
import app.servicedefinition.ServiceDefinition;
import app.servicedefinition.group.ServiceGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceRequestMapperTest {
    ServiceRequestMapper serviceRequestMapper = new ServiceRequestMapper(new ObjectMapper(), null);

    @Test
    void applyPatch_shouldUpdateStatus() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setStatus(ServiceRequestStatus.CLOSED);

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals(ServiceRequestStatus.CLOSED, serviceRequest.getStatus());
    }

    @Test
    void applyPatch_shouldUpdatePriority() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setPriority(ServiceRequestPriority.HIGH);

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals(ServiceRequestPriority.HIGH, serviceRequest.getPriority());
    }

    @Test
    void applyPatch_shouldUpdateAgencyEmail() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setAgencyEmail("new@agency.gov");

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals("new@agency.gov", serviceRequest.getAgencyEmail());
    }

    @Test
    void applyPatch_shouldUpdateServiceNotice() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setServiceNotice("Service notice updated");

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals("Service notice updated", serviceRequest.getServiceNotice());
    }

    @Test
    void applyPatch_shouldUpdateStatusNotes() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setStatusNotes("Work completed");

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals("Work completed", serviceRequest.getStatusNotes());
    }

    @Test
    void applyPatch_shouldUpdateAgencyResponsible() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setAgencyResponsible("Public Works Department");

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals("Public Works Department", serviceRequest.getAgencyResponsible());
    }

    @Test
    void applyPatch_shouldUpdateExpectedDate() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        Instant expectedDate = Instant.parse("2024-12-31T23:59:59Z");
        patchDTO.setExpectedDate(expectedDate);

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals(expectedDate, serviceRequest.getExpectedDate());
    }

    @Test
    void applyPatch_shouldUpdateClosedDate() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        Instant closedDate = Instant.parse("2024-11-01T10:00:00Z");
        patchDTO.setClosedDate(closedDate);

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals(closedDate, serviceRequest.getClosedDate());
    }

    @Test
    void applyPatch_shouldUpdateMultipleFields() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        patchDTO.setStatus(ServiceRequestStatus.IN_PROGRESS);
        patchDTO.setPriority(ServiceRequestPriority.MEDIUM);
        patchDTO.setStatusNotes("Under investigation");
        patchDTO.setAgencyResponsible("IT Department");

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        assertEquals(ServiceRequestStatus.IN_PROGRESS, serviceRequest.getStatus());
        assertEquals(ServiceRequestPriority.MEDIUM, serviceRequest.getPriority());
        assertEquals("Under investigation", serviceRequest.getStatusNotes());
        assertEquals("IT Department", serviceRequest.getAgencyResponsible());
    }

    @Test
    void applyPatch_shouldNotUpdateNullFields() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setPriority(ServiceRequestPriority.LOW);
        serviceRequest.setAgencyEmail("original@agency.gov");

        ServiceRequestUpdateRequest patchDTO = new ServiceRequestUpdateRequest();
        // Only set status, leave others null
        patchDTO.setStatus(ServiceRequestStatus.CLOSED);

        serviceRequestMapper.applyPatch(patchDTO, serviceRequest);

        // Status should be updated
        assertEquals(ServiceRequestStatus.CLOSED, serviceRequest.getStatus());
        // Others should remain unchanged
        assertEquals(ServiceRequestPriority.LOW, serviceRequest.getPriority());
        assertEquals("original@agency.gov", serviceRequest.getAgencyEmail());
    }

    private ServiceRequest createTestServiceRequest() {
        ServiceRequest serviceRequest = new ServiceRequest();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("test-jurisdiction");
        ServiceDefinition serviceDefinition = new ServiceDefinition("Test Service");
        serviceDefinition.setJurisdiction(jurisdiction);

        ServiceGroup serviceGroup = new ServiceGroup("Test Group", jurisdiction);
        serviceDefinition.setServiceGroup(serviceGroup);

        serviceRequest.setService(serviceDefinition);
        serviceRequest.setJurisdiction(jurisdiction);
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setPriority(ServiceRequestPriority.LOW);

        return serviceRequest;
    }
}
