package sg.edu.nus.comp.lms.layer;

import com.opencsv.CSVWriter;
import sg.edu.nus.comp.lms.aoi.AOI;
import sg.edu.nus.comp.lms.aoi.entity.UserAOI;
import sg.edu.nus.comp.lms.aoi.feature.*;
import sg.edu.nus.comp.lms.domain.Settings;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

public class AOILayerBuilder {

    private static final int K = 3;

    private static final AOIFeature[] AOI_FEATURES = {
            new Id(), new NumberOfAOI(), new Radius(), new IndependentVenues(), new CenterDistance()
    };

    public static void main(String[] args) throws Exception {
        AOI aoi = new AOI(new File(Settings.VORONTSOV_CHECKINS), K);
        List<UserAOI> userAOIs = aoi.build();

        try (CSVWriter writer = new CSVWriter(new FileWriter(Settings.MOBILITY_MODAL_FILE))) {
            writer.writeNext(Arrays.stream(AOI_FEATURES)
                    .map(AOIFeature::getName)
                    .toArray(String[]::new));
            for (UserAOI userPolygons : userAOIs) {
                writer.writeNext(Arrays.stream(AOI_FEATURES)
                        .map(feature -> feature.getValue(userPolygons))
                        .toArray(String[]::new));
            }
        }
    }
}
