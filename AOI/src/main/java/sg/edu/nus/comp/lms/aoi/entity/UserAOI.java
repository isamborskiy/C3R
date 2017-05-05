package sg.edu.nus.comp.lms.aoi.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class UserAOI {

    private final String id;
    private final List<List<GeographicalPoint>> points;
    private final double percentOfNoise;

    @JsonCreator
    public UserAOI(String id, List<List<GeographicalPoint>> points, double percentOfNoise) {
        this.id = id;
        this.points = points;
        this.percentOfNoise = percentOfNoise;
    }

    public String getId() {
        return id;
    }

    public List<List<GeographicalPoint>> getPoints() {
        return points;
    }

    @JsonIgnore
    public double getPercentOfNoise() {
        return percentOfNoise;
    }
}
