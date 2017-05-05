package sg.edu.nus.comp.lms.aoi;

import com.opencsv.CSVReader;
import sg.edu.nus.comp.lms.aoi.entity.CartesianPoint;
import sg.edu.nus.comp.lms.aoi.entity.GeographicalPoint;
import sg.edu.nus.comp.lms.aoi.entity.UserAOI;
import sg.edu.nus.comp.lms.aoi.util.DBScanUtils;
import sg.edu.nus.comp.lms.aoi.util.FastConvexHull;
import sg.edu.nus.comp.lms.domain.util.ArraysUtils;
import weka.clusterers.DBSCAN;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AOI {

    private static final String RELATION_NAME = "user_checkins";
    private static final String ID = "_id";
    private static final String LOCATION = "venue_location";

    private static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<>();
    private static final Attribute X_ATTRIBUTE = new Attribute("x");
    private static final Attribute Y_ATTRIBUTE = new Attribute("y");
    private static final Attribute Z_ATTRIBUTE = new Attribute("z");

    static {
        ATTRIBUTES.add(X_ATTRIBUTE);
        ATTRIBUTES.add(Y_ATTRIBUTE);
        ATTRIBUTES.add(Z_ATTRIBUTE);
    }

    private final File checkinsFile;
    private final int k;

    public AOI(File checkinsFile, int k) {
        this.checkinsFile = checkinsFile;
        this.k = k;
    }

    public List<UserAOI> build() throws Exception {
        Map<CartesianPoint, GeographicalPoint> pointsMapping = new HashMap<>();
        Map<String, List<CartesianPoint>> userCheckins = getCheckins(pointsMapping);
        return getUserAOI(pointsMapping, userCheckins);
    }

    private List<UserAOI> getUserAOI(Map<CartesianPoint, GeographicalPoint> pointsMapping,
                                     Map<String, List<CartesianPoint>> idToLocations) throws Exception {
        List<UserAOI> userAOIList = new ArrayList<>();
        FastConvexHull convexHull = new FastConvexHull();
        for (String id : idToLocations.keySet()) {
            Instances instances = makeInstances(idToLocations.get(id));
            List<List<Instance>> clusters = new ArrayList<>();
            int noise = clusterInstances(instances, clusters);
            List<List<GeographicalPoint>> userAOI = new ArrayList<>();
            clusters.stream()
                    .forEach(cluster -> {
                        List<CartesianPoint> points = cluster.stream()
                                .map(instance -> new CartesianPoint(instance.value(X_ATTRIBUTE),
                                        instance.value(Y_ATTRIBUTE), instance.value(Z_ATTRIBUTE)))
                                .collect(Collectors.toList());
                        points = convexHull.execute(points);
                        userAOI.add(points.stream()
                                .map(pointsMapping::get)
                                .collect(Collectors.toList()));
                    });
            UserAOI userPolygons = new UserAOI(id, userAOI, (double) noise / idToLocations.get(id).size());
            userAOIList.add(userPolygons);
        }
        return userAOIList;
    }

    private Map<String, List<CartesianPoint>> getCheckins(Map<CartesianPoint, GeographicalPoint> pointsMapping) throws IOException {
        Map<String, List<CartesianPoint>> idToLocations = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(checkinsFile))) {
            List<String[]> data = reader.readAll();
            String[] header = data.remove(0);
            int idIndex = ArraysUtils.indexOf(header, ID);
            int locationIndex = ArraysUtils.indexOf(header, LOCATION);

            for (String[] row : data) {
                String id = row[idIndex];
                GeographicalPoint coordinates = new GeographicalPoint(row[locationIndex]);
                CartesianPoint point = coordinates.toCartesianPoint();

                pointsMapping.put(point, coordinates);
                List<CartesianPoint> points = idToLocations.getOrDefault(id, new ArrayList<>());
                points.add(point);
                idToLocations.put(id, points);
            }
        }
        return idToLocations;
    }

    private int clusterInstances(Instances instances, List<List<Instance>> clusters) throws Exception {
        DBSCAN dbscan = new DBSCAN();
        dbscan.setEpsilon(DBScanUtils.findEpsilon(instances, k));
        dbscan.setMinPoints(k);
        dbscan.buildClusterer(instances);

        IntStream.range(0, dbscan.numberOfClusters()).forEach(i -> clusters.add(new ArrayList<>()));
        int noise = 0;
        for (Instance instance : instances) {
            try {
                int clusterId = dbscan.clusterInstance(instance);
                clusters.get(clusterId).add(instance);
            } catch (Exception ignored) { // NOISE
                noise++;
            }
        }
        return noise;
    }

    private Instances makeInstances(List<CartesianPoint> checkins) {
        Set<CartesianPoint> points = new HashSet<>(checkins);
        Instances instances = new Instances(RELATION_NAME, ATTRIBUTES, points.size());
        for (CartesianPoint point : points) {
            Instance instance = new DenseInstance(ATTRIBUTES.size());
            instance.setValue(X_ATTRIBUTE, point.x);
            instance.setValue(Y_ATTRIBUTE, point.y);
            instance.setValue(Z_ATTRIBUTE, point.z);
            instances.add(instance);
        }
        return instances;
    }
}