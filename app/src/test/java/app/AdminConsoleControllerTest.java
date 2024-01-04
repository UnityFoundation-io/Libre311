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

import app.dto.servicerequest.PostRequestServiceRequestDTO;
import app.dto.servicerequest.PostResponseServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.model.service.ServiceRepository;
import app.model.servicerequest.ServiceRequestRepository;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockReCaptchaService;
import app.util.MockSecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.micronaut.http.HttpStatus.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments={"app-api-test-data"})
//@MicronautTest
public class AdminConsoleControllerTest {

    @Inject
    @Client("/api/admin")
    HttpClient adminClient;

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
        dbCleanup.cleanupJurisdictions();
        dbCleanup.cleanupServiceRequests();
        mockAuthenticationFetcher.setAuthentication(null);
    }

    void login() {
        mockAuthenticationFetcher.setAuthentication(mockSecurityService.getAuthentication().get());
    }


    // todo: WIP update
//    @Test
//    public void canUpdateIfAuthenticated() {
//        HttpResponse<?> response;
//
//        response = createServiceRequest("001", "12345 Fairway",
//                Map.of("attribute[SDWLK]", "NARROW"));
//        assertEquals(HttpStatus.OK, response.getStatus());
//        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
//        assertTrue(optional.isPresent());
//        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
//        assertTrue(Arrays.stream(postResponseServiceRequestDTOS)
//                .anyMatch(postResponseServiceRequestDTO -> postResponseServiceRequestDTO.getId() != null));
//
//        // update attempt
//        // create service requests
//
//
//
//    }

    // read
    @Test
    public void canReadSensitiveInfoIfAuthenticated() {
        HttpResponse<?> response;

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"));
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();

        PostResponseServiceRequestDTO postResponseServiceRequestDTO = postResponseServiceRequestDTOS[0];

        // unauthenticated read attempt
        HttpRequest<?> request = HttpRequest.GET("/requests/" + postResponseServiceRequestDTO.getId());

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            adminClient.toBlocking().exchange(request, SensitiveServiceRequestDTO[].class);
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        login();

        response = adminClient.toBlocking().exchange("/requests/" + postResponseServiceRequestDTO.getId(), SensitiveServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<SensitiveServiceRequestDTO[]> bodyOptional = response.getBody(SensitiveServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        SensitiveServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
    }

    private HttpResponse<?> createServiceRequest(String serviceCode, String address, Map attributes) {
        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO(serviceCode);
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
