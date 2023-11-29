// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app;

import app.dto.service.ServiceDTO;
import app.dto.servicerequest.PostRequestServiceRequestDTO;
import app.dto.servicerequest.PostResponseServiceRequestDTO;
import app.dto.servicerequest.ServiceRequestDTO;
import app.model.service.ServiceRepository;
import app.model.servicerequest.ServiceRequestRepository;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockReCaptchaService;
import app.util.MockSecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static io.micronaut.http.HttpStatus.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments={"test-jurisdiction-support"})
public class JurisdictionSupportRootControllerTest {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockReCaptchaService mockReCaptchaService;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    MockSecurityService mockSecurityService;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @Inject
    DbCleanup dbCleanup;

    @BeforeEach
    void setup() {
        dbCleanup.cleanupServiceRequests();
        mockAuthenticationFetcher.setAuthentication(null);
    }

    void login() {
        mockAuthenticationFetcher.setAuthentication(mockSecurityService.getAuthentication().get());
    }

    // create
    @Test
    public void canCreateServiceRequestThatDoesNotRequireAdditionalAttributes() {
        HttpResponse<?> response;

        response = createServiceRequest("206", "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        assertTrue(Arrays.stream(postResponseServiceRequestDTOS)
                .anyMatch(postResponseServiceRequestDTO -> postResponseServiceRequestDTO.getId() != null));
    }

    @Test
    public void canCreateServiceRequestWithRequiredAttributes() {
        HttpResponse<?> response;

        response = createServiceRequest("201", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        assertTrue(Arrays.stream(postResponseServiceRequestDTOS)
                .anyMatch(postResponseServiceRequestDTO -> postResponseServiceRequestDTO.getId() != null));
    }

    @Test
    public void cannotCreateServiceRequestWithoutRequiredAttributes() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest("201", "12345 Fairway", Map.of(), "city.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestIfAddressIsNotProvided() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest("206", null, Map.of(), "town.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    // list + read
    @Test
    public void canListServicesByJurisdictionId() {
        HttpResponse<?> response;

        response = client.toBlocking().exchange("/services?jurisdiction_id=city.gov", ServiceDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceDTO[]> bodyOptional = response.getBody(ServiceDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceDTO[] serviceDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceDTOS).findAny().isPresent());
    }

    @Test
    public void canGetServiceDefinitionByServiceCodeAndRequiredJurisdictionId() {
        HttpResponse<?> response;

        response = client.toBlocking().exchange("/services/201?jurisdiction_id=city.gov", String.class);
        assertEquals(HttpStatus.OK, response.status());
        Optional<String> serviceDefinitionOptional = response.getBody(String.class);
        assertTrue(serviceDefinitionOptional.isPresent());
        String serviceDefinition = serviceDefinitionOptional.get();
        assertTrue(StringUtils.hasText(serviceDefinition));
    }

    @Test
    public void canListServiceRequestsByJurisdiction() {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("206", "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        //
        response = client.toBlocking().exchange("/requests?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceRequestDTO[]> bodyOptional = response.getBody(ServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
        assertTrue(Arrays.stream(serviceRequestDTOS).allMatch(
                serviceRequestDTO -> "city.gov".equals(serviceRequestDTO.getJurisdictionId())));
    }

    @Test
    public void canGetAServiceRequestWithJurisdictionId() {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("201", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        PostResponseServiceRequestDTO postResponseServiceRequestDTO = postResponseServiceRequestDTOS[0];

        response = client.toBlocking().exchange("/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceRequestDTO[]> bodyOptional = response.getBody(ServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
    }

    @Test
    public void canDownloadCSVFile() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("206", "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        // create service requests
        HttpRequest<?> request = HttpRequest.GET("/requests/download");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, byte[].class);
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        login();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());
        InputStream inputStream = new ByteArrayInputStream(body.get());
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertNotNull(text);
    }

    @Test
    public void theCSVFileShouldNotContainCellsBeginningWithUnsafeCharacters() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("201", "=1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "@1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "+1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "-1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "\t1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("201", "\r1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        // create service requests
        HttpRequest<?> request = HttpRequest.GET("/requests/download");

        login();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());
        InputStream inputStream = new ByteArrayInputStream(body.get());
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertNotNull(text);
        try (CSVReader reader = new CSVReader(new StringReader(text))) {
            reader.skip(1); // skip header
            String[] cells = reader.readNext();
            while (cells != null) {
                String addressCell = cells[0];
                assertTrue(addressCell.startsWith("'"));
                cells = reader.readNext();
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<?> createServiceRequest(String serviceCode, String address, Map attributes, String jurisdictionId) {
        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO(serviceCode);
        serviceRequestDTO.setJurisdictionId(jurisdictionId);
        serviceRequestDTO.setgRecaptchaResponse("abc");
        if (address != null) {
            serviceRequestDTO.setAddressString(address);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceRequestDTO, Map.class);
        payload.putAll(attributes);
        HttpRequest<?> request = HttpRequest.POST("/requests", payload)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, Map.class);
    }
}
