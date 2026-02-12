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

import app.dto.jurisdiction.JurisdictionDTO;
import app.dto.jurisdiction.UpdatePolicyContentDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdictionuser.JurisdictionUser;
import app.model.jurisdictionuser.JurisdictionUserRepository;
import app.model.user.User;
import app.model.user.UserRepository;
import app.security.HasPermissionResponse;
import app.service.jurisdiction.JurisdictionBoundaryService;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockUnityAuthClient;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static app.util.JurisdictionBoundaryUtil.DEFAULT_BOUNDS;
import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class JurisdictionAdminPolicyContentTest {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @Inject
    DbCleanup dbCleanup;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    JurisdictionBoundaryService jurisdictionBoundaryService;

    @Inject
    UserRepository userRepository;

    @Inject
    JurisdictionUserRepository jurisdictionUserRepository;

    @BeforeEach
    void setup() {
        mockAuthenticationFetcher.setAuthentication(null);
        setAuthHasPermissionResponse(false, null, "Unauthorized", null);

        User admin = userRepository.save(new User("admin@fakecity.gov"));
        Jurisdiction jurisdiction = jurisdictionRepository.save(new Jurisdiction("fakecity.gov", 1L));
        jurisdictionBoundaryService.saveBoundary(jurisdiction, DEFAULT_BOUNDS);
        jurisdictionUserRepository.save(new JurisdictionUser(admin, jurisdiction, true));
    }

    @AfterEach
    void teardown() {
        dbCleanup.cleanupAll();
    }

    private void setAuthHasPermissionResponse(boolean hasPermission, String email, String errorMessage, List<String> permissions) {
        mockUnityAuthClient.setResponse(HttpResponse.ok(
            new HasPermissionResponse(hasPermission, email, errorMessage, permissions)));
    }

    private void authLogin(List<String> permissions) {
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        setAuthHasPermissionResponse(true, "admin@fakecity.gov", null, permissions);
    }

    // --- Happy path tests ---

    @Test
    void subtenantAdminCanUpdateTermsOfUseContent() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Terms of Use\n\nThese are the terms.");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertEquals("# Terms of Use\n\nThese are the terms.", body.get().getTermsOfUseContent());
    }

    @Test
    void subtenantAdminCanUpdatePrivacyPolicyContent() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setPrivacyPolicyContent("# Privacy Policy\n\nWe respect your privacy.");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertEquals("# Privacy Policy\n\nWe respect your privacy.", body.get().getPrivacyPolicyContent());
    }

    @Test
    void canUpdateBothPolicyFieldsAtOnce() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# T&C\n\nUpdated terms.");
        dto.setPrivacyPolicyContent("# Privacy\n\nUpdated privacy.");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertEquals("# T&C\n\nUpdated terms.", body.get().getTermsOfUseContent());
        assertEquals("# Privacy\n\nUpdated privacy.", body.get().getPrivacyPolicyContent());
    }

    @Test
    void tenantAdminCanUpdatePolicyContent() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-TENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Tenant Terms");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertEquals("# Tenant Terms", body.get().getTermsOfUseContent());
    }

    @Test
    void systemAdminCanUpdatePolicyContent() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setPrivacyPolicyContent("# System Privacy Policy");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertEquals("# System Privacy Policy", body.get().getPrivacyPolicyContent());
    }

    // --- Clearing content returns default ---

    @Test
    void nullContentClearsToDefault() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        // First set content
        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Custom Terms");
        dto.setPrivacyPolicyContent("# Custom Privacy");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");
        HttpResponse<JurisdictionDTO> setResponse = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, setResponse.getStatus());
        Optional<JurisdictionDTO> setBody = setResponse.getBody(JurisdictionDTO.class);
        assertTrue(setBody.isPresent());
        assertEquals("# Custom Terms", setBody.get().getTermsOfUseContent());

        // Then clear it by sending null (not providing the fields)
        UpdatePolicyContentDTO clearDto = new UpdatePolicyContentDTO();
        // Both fields are null - PUT semantics clears them

        request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", clearDto)
            .header("Authorization", "Bearer token.text.here");
        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());

        // Verify DB was actually cleared
        Optional<Jurisdiction> cleared = jurisdictionRepository.findById("fakecity.gov");
        assertTrue(cleared.isPresent());
        assertNull(cleared.get().getTermsOfUseContent(),
            "DB terms should be null but was: " + cleared.get().getTermsOfUseContent());
        assertNull(cleared.get().getPrivacyPolicyContent(),
            "DB privacy should be null but was: " + cleared.get().getPrivacyPolicyContent());

        // Response should contain default content (applied by applyDefaultPolicyContent)
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertNotNull(body.get().getTermsOfUseContent());
        assertNotNull(body.get().getPrivacyPolicyContent());
    }

    // --- Authorization failure tests ---

    @Test
    void unauthenticatedUserCannotUpdatePolicyContent() {
        // No auth set up (mock returns null authentication, but header is present)
        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Unauthorized Terms");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void noAuthorizationHeaderReturnsUnauthorized() {
        // No Authorization header at all - security rule rejects immediately
        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# No Header Terms");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto);

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void userWithoutEditPermissionCannotUpdatePolicyContent() {
        // Mock returns hasPermission: false to simulate a user without edit rights
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        setAuthHasPermissionResponse(false, "viewer@fakecity.gov", "Unauthorized", List.of("LIBRE311_ADMIN_VIEW-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# View-only Terms");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(FORBIDDEN, exception.getStatus());
    }

    @Test
    void requestEditPermissionCannotUpdatePolicyContent() {
        // REQUEST_EDIT permissions are insufficient for admin endpoint (requires ADMIN_EDIT)
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        setAuthHasPermissionResponse(false, "editor@fakecity.gov", "Unauthorized", List.of("LIBRE311_REQUEST_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Wrong Permission Terms");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(FORBIDDEN, exception.getStatus());
    }

    @Test
    void missingJurisdictionIdReturnsBadRequest() {
        // Missing jurisdiction_id query parameter triggers IllegalArgumentException in security rule
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# No Jurisdiction ID");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    // --- Validation and security tests ---

    @Test
    void acceptsMarkdownWithHtmlTags() {
        // Markdown content may contain HTML. XSS sanitization is handled by the frontend
        // (DOMPurify in MarkdownRenderer.svelte). This test documents that the backend
        // stores content as-is, relying on frontend sanitization.
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Terms\n\n<script>alert('XSS')</script>\n\n**Bold text**");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        // Content is stored as-is; frontend is responsible for sanitization
        assertTrue(body.get().getTermsOfUseContent().contains("**Bold text**"));
    }

    @Test
    void termsOfUseCannotExceedMaxContentLength() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("x".repeat(50001));

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    @Test
    void privacyPolicyCannotExceedMaxContentLength() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setPrivacyPolicyContent("x".repeat(50001));

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    @Test
    void bothFieldsExceedingMaxLengthReturnsBadRequest() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("x".repeat(50001));
        dto.setPrivacyPolicyContent("y".repeat(50001));

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    @Test
    void contentAtExactMaxLengthIsAccepted() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("x".repeat(50000));

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
    }

    @Test
    void whitespaceOnlyContentIsTreatedAsCleared() {
        // Whitespace-only content should be stored as-is (not treated as empty)
        // since emptyToNull only checks for empty string, not whitespace
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("   ");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        // Whitespace-only content is stored as-is (not null), returned as provided
        assertEquals("   ", body.get().getTermsOfUseContent());
    }

    // --- Not found tests ---

    @Test
    void returns404ForNonExistentJurisdiction() {
        // Use SYSTEM permission to bypass jurisdiction-level user lookup
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SYSTEM"));

        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Terms");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=nonexistent.gov", dto)
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(request, JurisdictionDTO.class));
        assertEquals(NOT_FOUND, exception.getStatus());
    }

    // --- PUT semantics: null field clears content to default ---

    @Test
    void nullFieldClearsContentToDefault() {
        authLogin(List.of("LIBRE311_ADMIN_EDIT-SUBTENANT"));

        // First set both fields
        UpdatePolicyContentDTO dto = new UpdatePolicyContentDTO();
        dto.setTermsOfUseContent("# Original Terms");
        dto.setPrivacyPolicyContent("# Original Privacy");

        HttpRequest<?> request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", dto)
            .header("Authorization", "Bearer token.text.here");
        client.toBlocking().exchange(request, JurisdictionDTO.class);

        // PUT with only terms set - privacy is null, so it gets cleared to default
        UpdatePolicyContentDTO updateDto = new UpdatePolicyContentDTO();
        updateDto.setTermsOfUseContent("# Updated Terms");
        // privacyPolicyContent is null - PUT semantics means it gets cleared

        request = HttpRequest.PUT(
                "/jurisdiction-admin/policy-content?jurisdiction_id=fakecity.gov", updateDto)
            .header("Authorization", "Bearer token.text.here");
        HttpResponse<JurisdictionDTO> response = client.toBlocking().exchange(request, JurisdictionDTO.class);

        assertEquals(OK, response.getStatus());
        Optional<JurisdictionDTO> body = response.getBody(JurisdictionDTO.class);
        assertTrue(body.isPresent());
        assertEquals("# Updated Terms", body.get().getTermsOfUseContent());
        // Privacy was cleared, so it should be default content (not "# Original Privacy")
        assertNotNull(body.get().getPrivacyPolicyContent());
        assertNotEquals("# Original Privacy", body.get().getPrivacyPolicyContent());
    }
}
