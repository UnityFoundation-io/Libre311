package app.model.jurisdiction;

import io.micronaut.core.annotation.NonNull;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "jurisdiction_boundary")
public class JurisdictionBoundaryEntity implements JurisdictionBoundary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Polygon boundary;

    String jurisdictionId;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Polygon getBoundary() {
        return boundary;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
    }

    @Override
    @NonNull
    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(@NonNull String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public static JurisdictionBoundaryEntity from(String jurisdictionId, Polygon boundary) {
        JurisdictionBoundaryEntity jb = new JurisdictionBoundaryEntity();
        jb.setJurisdictionId(jurisdictionId);
        jb.setBoundary(boundary);

        return jb;
    }
}
