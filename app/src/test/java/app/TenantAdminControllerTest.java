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

import app.dto.jurisdiction.CreateJurisdictionDTO;
import app.dto.jurisdiction.JurisdictionDTO;
import app.dto.jurisdiction.PatchJurisdictionDTO;
import app.security.HasPermissionResponse;
import app.util.MockAuthenticationFetcher;
import app.util.MockUnityAuthClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.*;
import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class TenantAdminControllerTest {

    @Inject
    @Client("/api/tenant-admin")
    HttpClient client;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @BeforeEach
    void setup() {
        // login
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
    }

    private void setAuthHasPermissionResponse(boolean hasPermission, String email, String errorMessage, List<String> permissions) {
        mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(hasPermission, email, errorMessage, permissions)));
    }

    @Test
    public void jurisdictionAdminCannotCreateAJurisdiction() {
        setAuthHasPermissionResponse(true, "jurisdictionAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("george.town");
        createJurisdictionDTO.setName("City of Georgetown");

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, JurisdictionDTO.class);
        });
        assertEquals(FORBIDDEN, exception.getStatus());
    }

    @Test
    public void tenantAdminCanCreateAJurisdiction() {
        setAuthHasPermissionResponse(true, "tenantAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-TENANT"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("jorge.town");
        createJurisdictionDTO.setName("City of Jorgetown");

        Double[][] bound = {
                {-90.30025693587594, 38.68777201455936},
                {-90.34433315946103, 38.61729515893717},
                {-90.2360192439154, 38.61061640035771},
                {-90.20658055119375, 38.68231721278784},
                {-90.30025693587594, 38.68777201455936}
        };
        createJurisdictionDTO.setBounds(bound);

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
    }

    @Test
    public void systemAdminCanCreateAJurisdiction() {
        setAuthHasPermissionResponse(true, "systemAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("louisville.city");
        createJurisdictionDTO.setName("City of Louisville");
        createJurisdictionDTO.setPrimaryColor("221, 83%, 53%");
        createJurisdictionDTO.setLogoMediaUrl("http://example.com/img/here");

        Double[][] bounds = {
                {-90.30025693587594, 38.68777201455936},
                {-90.34433315946103, 38.61729515893717},
                {-90.2360192439154, 38.61061640035771},
                {-90.20658055119375, 38.68231721278784},
                {-90.30025693587594, 38.68777201455936}
        };
        createJurisdictionDTO.setBounds(bounds);

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> optional = response.getBody(JurisdictionDTO.class);
        assertTrue(optional.isPresent());
        JurisdictionDTO jurisdictionDTO = optional.get();
        assertEquals("louisville.city", jurisdictionDTO.getJurisdictionId());
        assertEquals("221, 83%, 53%", jurisdictionDTO.getPrimaryColor());
        assertEquals("http://example.com/img/here", jurisdictionDTO.getLogoMediaUrl());
    }

    @Test
    public void cannotCanCreateAJurisdictionWithUntrustedCSSColorValue() {
        setAuthHasPermissionResponse(true, "systemAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("luisville.city");
        createJurisdictionDTO.setName("City of Luisville");
        createJurisdictionDTO.setPrimaryColor("<SCRIPT type=\"text/javascript\">\n" + "var adr = '../evil.php?cakemonster=' + escape(document.cookie);\n" + "</SCRIPT>");

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, JurisdictionDTO.class);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void cantUpdateRemoteHostsBadAuth() {
        setAuthHasPermissionResponse(true, "systemAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));
        // Can set remote hosts
        HttpRequest<?> request = HttpRequest.POST("/jurisdictions/neverland/remote_hosts?tenant_id=1", Set.of("foo", "bar"))
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, JurisdictionDTO.class);
        });
        assertEquals(FORBIDDEN, exception.getStatus());
    }

    @Test
    public void cantUpdateRemoteHostsOnNonExistentJurisdiction() {
        setAuthHasPermissionResponse(true, "systemAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));
        // Can set remote hosts
        HttpRequest<?> request = HttpRequest.POST("/jurisdictions/neverland/remote_hosts?tenant_id=1", Set.of("foo", "bar"))
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, JurisdictionDTO.class);
        });
        assertEquals(NOT_FOUND, exception.getStatus());
    }

    @Test
    public void canUpdateAJurisdiction() {
        setAuthHasPermissionResponse(true, "systemAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("ogdenville.city");
        createJurisdictionDTO.setName("City of Ogdenville");
        createJurisdictionDTO.setPrimaryColor("221, 83%, 53%");

        Double[][] bound = {
                {-90.30025693587594, 38.68777201455936},
                {-90.34433315946103, 38.61729515893717},
                {-90.2360192439154, 38.61061640035771},
                {-90.20658055119375, 38.68231721278784},
                {-90.30025693587594, 38.68777201455936}
        };
        createJurisdictionDTO.setBounds(bound);

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());

        Optional<JurisdictionDTO> optional = response.getBody(JurisdictionDTO.class);
        assertTrue(optional.isPresent());
        JurisdictionDTO jurisdictionDTO = optional.get();
        assertTrue(jurisdictionDTO.getBounds().length > 0);
        assertTrue(Arrays.deepEquals(bound, jurisdictionDTO.getBounds()));

        // update
        PatchJurisdictionDTO patchJurisdictionDTO = new PatchJurisdictionDTO();
        patchJurisdictionDTO.setName("Ogdenville - America's Barley Basket");
        patchJurisdictionDTO.setPrimaryColor("221, 83%, 53%");
        Double[][] updateBound = {
                {-90.30025693587594, 38.68777201455936},
                {-90.34433315946103, 38.61729515893717},
                {-90.2360192439154, 38.61061640035771},
                {-90.22097070151844, 38.7066943276854},
                {-90.30025693587594, 38.68777201455936}
        };
        patchJurisdictionDTO.setBounds(updateBound);

        request = HttpRequest.PATCH("/jurisdictions/ogdenville.city?tenant_id=1", patchJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> jurisdictionDTOOptional = response.getBody(JurisdictionDTO.class);
        assertTrue(jurisdictionDTOOptional.isPresent());
        jurisdictionDTO = jurisdictionDTOOptional.get();
        assertEquals("Ogdenville - America's Barley Basket", jurisdictionDTO.getName());
        assertEquals("221, 83%, 53%", jurisdictionDTO.getPrimaryColor());
        assertTrue(jurisdictionDTO.getBounds().length > 0);
        assertEquals(5, jurisdictionDTO.getBounds().length);
        assertTrue(Arrays.deepEquals(updateBound, jurisdictionDTO.getBounds()));

        // Can set remote hosts
        request = HttpRequest.POST("/jurisdictions/ogdenville.city/remote_hosts?tenant_id=1", Set.of("foo", "bar"))
                .header("Authorization", "Bearer token.text.here");

        response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        jurisdictionDTOOptional = response.getBody(JurisdictionDTO.class);
        assertTrue(jurisdictionDTOOptional.isPresent());
        jurisdictionDTO = jurisdictionDTOOptional.get();
        assertEquals("Ogdenville - America's Barley Basket", jurisdictionDTO.getName());
        assertEquals("221, 83%, 53%", jurisdictionDTO.getPrimaryColor());
        assertTrue(jurisdictionDTO.getBounds().length > 0);
        assertEquals(5, jurisdictionDTO.getBounds().length);
        assertTrue(Arrays.deepEquals(updateBound, jurisdictionDTO.getBounds()));
        assertEquals(2, jurisdictionDTO.getRemoteHosts().size());
        assertTrue(jurisdictionDTO.getRemoteHosts().containsAll(List.of("foo", "bar")));
    }

    @Test
    public void cannotCreateJurisdictionIfBoundIsNotClosed() {
        setAuthHasPermissionResponse(true, "systemAdmin@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("ogdenville.town");
        createJurisdictionDTO.setName("City of Ogdenville");
        createJurisdictionDTO.setPrimaryColor("221, 83%, 53%");

        // first and last are not the same
        Double[][] bound = {
                {-90.30025693587594, 38.68777201455936},
                {-90.34433315946103, 38.61729515893717},
                {-90.2360192439154, 38.61061640035771},
                {-90.22097070151844, 38.7066943276854},
                {-90.30025693587594, 38.68777201455937}
        };
        createJurisdictionDTO.setBounds(bound);

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, JurisdictionDTO.class);
        });
        assertEquals(INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    public void cannotCreateJurisdictionIfBoundsConsistOfNonTuples() {
        setAuthHasPermissionResponse(true, "systemAdmint@test.io", null, List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        CreateJurisdictionDTO createJurisdictionDTO = new CreateJurisdictionDTO();
        createJurisdictionDTO.setJurisdictionId("springfield.city");
        createJurisdictionDTO.setName("City of Springfield");
        createJurisdictionDTO.setPrimaryColor("221, 83%, 53%");

        // not tuples
        Double[][] bound = {
                {-90.30025693587594, 38.68777201455936, 38.68777201455936},
                {-90.34433315946103, 38.61729515893717},
                {-90.2360192439154, 38.61061640035771},
                {-90.22097070151844, 38.7066943276854},
                {-90.30025693587594, 38.68777201455936}
        };
        createJurisdictionDTO.setBounds(bound);

        HttpRequest<?> request = HttpRequest.POST("/jurisdictions?tenant_id=1", createJurisdictionDTO)
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, JurisdictionDTO.class);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());
    }
}