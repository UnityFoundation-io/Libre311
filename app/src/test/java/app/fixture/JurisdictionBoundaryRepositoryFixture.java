package app.fixture;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionBoundaryEntity;
import app.model.jurisdiction.JurisdictionBoundaryRepository;
import app.model.service.group.ServiceGroupRepository;
import app.service.geometry.LibreGeometryFactory;
import jakarta.inject.Inject;
import org.locationtech.jts.geom.Coordinate;

public class JurisdictionBoundaryRepositoryFixture extends JurisdictionRepositoryFixture {

    @Inject
    public JurisdictionBoundaryRepository jurisdictionBoundaryRepository;

    @Inject
    public LibreGeometryFactory libreGeometryFactory;


    public void cleanup(){
        super.cleanup();
        jurisdictionBoundaryRepository.deleteAll();
    }

    // a large boundary roughly covering St. Louis metro area (format [lat, lng])
    public static final Double[][] DEFAULT_BOUNDS = new Double[][]{
        new Double[]{38.88908245157475, -90.82207996696539},
        new Double[]{38.28511105115126, -90.32668241294714},
        new Double[]{38.73098601356233, -89.86006757704696},
        new Double[]{39.04413540068816, -90.36058752072049},
        new Double[]{38.88908245157475, -90.82207996696539},
    };

    public static final Coordinate IN_BOUNDS_COORDINATE = new Coordinate(-90.29517238194957,
        38.689033913397765);

    public JurisdictionBoundaryEntity saveJurisdictionBoundary(Jurisdiction jurisdiction,
        Double[][] bounds) {
        return jurisdictionBoundaryRepository.save(
            JurisdictionBoundaryEntity.from(jurisdiction.getId(),
                libreGeometryFactory.createPolygon(bounds)));
    }

}
