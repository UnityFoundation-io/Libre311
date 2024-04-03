package app.model.jurisdiction;

import org.locationtech.jts.geom.Polygon;

public interface JurisdictionBoundary {
    Long getId();

    Polygon getBoundary();

    String getJurisdictionId();
}
