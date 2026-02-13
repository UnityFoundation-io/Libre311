package app.service.geometry;

import app.exception.Libre311BaseException;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import java.util.Arrays;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

@Singleton
public class LibreGeometryFactory extends GeometryFactory {

    static class InvalidCoordinateException extends Libre311BaseException {

        public InvalidCoordinateException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    public LibreGeometryFactory() {
        super(new PrecisionModel(), 4326);
    }

    /**
     *
     * @param coordinates where each coordinate is [lat, lng]
     * @return a Polygon with no holes
     */
    public Polygon createPolygon(Double[][] coordinates) {
        Double[][] closedCoordinates = coordinates;
        if (coordinates.length > 0 && !Arrays.equals(coordinates[0], coordinates[coordinates.length - 1])) {
            closedCoordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
            closedCoordinates[closedCoordinates.length - 1] = coordinates[0];
        }

        Coordinate[] exteriorCoords = Arrays.stream(closedCoordinates)
            .map(LibreGeometryFactory::mapToCoordinate)
            .toArray(Coordinate[]::new);

        return this.createPolygon(this.createLinearRing(exteriorCoords), null);
    }

    public Point createPoint(String lat, String lng){
        return this.createPoint(new Coordinate(Double.parseDouble(lng), Double.parseDouble(lat)));
    }

    /**
     *
     * @param polygon a polygon
     * @return the coordinates of the outer polygon shell. Each coordinate is represented as [lat, lng]
     */
    public static Double[][] getCoordinatesFrom(Polygon polygon){
        return Arrays.stream(polygon.getCoordinates())
            .map(LibreGeometryFactory::mapToTuple)
            .toArray(Double[][]::new);
    }


    /**
     *
     * @param coord
     * @return [lat, lng]
     */
    private static Double[] mapToTuple(Coordinate coord){
        return new Double[]{coord.getY(), coord.getX()};
    }

    /**
     *
     * @param tuple representing lat and long in format [lat, lng]
     * @return Coordinate representation of the LatLng tuple
     */
    private static Coordinate mapToCoordinate(Double[] tuple){
        if (tuple.length != 2) {
            throw new InvalidCoordinateException("Coordinate tuple must be length of two");
        }
        return new Coordinate(tuple[1], tuple[0]);
    }
}
