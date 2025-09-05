package app.service.jurisdiction;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionBoundary;
import app.model.jurisdiction.JurisdictionBoundaryEntity;
import app.model.jurisdiction.JurisdictionBoundaryRepository;
import app.service.geometry.LibreGeometryFactory;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@Singleton
public class JurisdictionBoundaryService {

    LibreGeometryFactory libreGeometryFactory;
    JurisdictionBoundaryRepository jurisdictionBoundaryRepository;

    public JurisdictionBoundaryService(LibreGeometryFactory libreGeometryFactory,
        JurisdictionBoundaryRepository jurisdictionBoundaryRepository) {
        this.libreGeometryFactory = libreGeometryFactory;
        this.jurisdictionBoundaryRepository = jurisdictionBoundaryRepository;
    }

    public boolean existsInJurisdiction(String jurisdictionId, double lat, double lng){
        JurisdictionBoundaryEntity jb = jurisdictionBoundaryRepository.findByJurisdictionId(jurisdictionId);
        Point point = libreGeometryFactory.createPoint(new Coordinate(lng, lat));

        return jb.getBoundary().covers(point);
    }

    public JurisdictionBoundary saveBoundary(Jurisdiction jurisdiction,
        Double[][] bounds) {
        Polygon polygon = libreGeometryFactory.createPolygon(bounds);
        return jurisdictionBoundaryRepository.save(
            JurisdictionBoundaryEntity.from(jurisdiction.getId(), polygon));
    }

    @Transactional
    public JurisdictionBoundary updateBoundary(Jurisdiction jurisdiction, Double[][] bounds) {
        var jurisdictionBoundary = jurisdictionBoundaryRepository.findByJurisdictionId(jurisdiction.getId());
        jurisdictionBoundary.setBoundary(libreGeometryFactory.createPolygon(bounds));
        return jurisdictionBoundaryRepository.update(jurisdictionBoundary);
    }
}
