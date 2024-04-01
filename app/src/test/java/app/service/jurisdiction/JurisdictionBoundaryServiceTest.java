package app.service.jurisdiction;


import static app.util.JurisdictionBoundaryUtil.DEFAULT_BOUNDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionBoundary;
import app.model.jurisdiction.JurisdictionBoundaryRepository;
import app.model.jurisdiction.JurisdictionRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
class JurisdictionBoundaryServiceTest  {


    @Inject
    JurisdictionRepository jurisdictionRepository;
    @Inject
    JurisdictionBoundaryService jurisdictionBoundaryService;
    @Inject
    JurisdictionBoundaryRepository jurisdictionBoundaryRepository;

    Jurisdiction savedJurisdiction;

    @BeforeEach
    void setup() {
        savedJurisdiction = jurisdictionRepository.save(new Jurisdiction("stlma", 1L));
    }

    @AfterEach
    void teardown() {
        jurisdictionRepository.deleteAll();
    }

    @Test
    void canSaveJurisdictionBoundary() {
        JurisdictionBoundary boundary = jurisdictionBoundaryService.saveBoundary(savedJurisdiction,
            DEFAULT_BOUNDS);

        assertNotNull(jurisdictionBoundaryRepository.findById(boundary.getId()));
    }

    @Test
    void cannotSaveWhenBoundsAreInvalid() {
        Double[][] invalidBounds = new Double[][]{new Double[]{40.7128, -74.0060},
            new Double[]{40.7128, -73.9957}, new Double[]{40.7211, -73.9957},
            new Double[]{40.7211, -74.0060},};

        assertThrows(IllegalArgumentException.class,
            () -> jurisdictionBoundaryService.saveBoundary(savedJurisdiction, invalidBounds));
    }


    @Test
    void pointExistsInJurisdiction() {
        jurisdictionBoundaryService.saveBoundary(savedJurisdiction, DEFAULT_BOUNDS);
        assertTrue(jurisdictionBoundaryService.existsInJurisdiction(savedJurisdiction.getId(),
            38.689033913397765, -90.29517238194957));

    }

    @Test
    void pointDoesNotExistInJurisdiction() {
        jurisdictionBoundaryService.saveBoundary(savedJurisdiction, DEFAULT_BOUNDS);
        assertFalse(jurisdictionBoundaryService.existsInJurisdiction(savedJurisdiction.getId(),
            39.129514523061744, -94.74894776569042));

    }

}