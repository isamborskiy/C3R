package sg.edu.nus.comp.lms.domain.profiling;

import sg.edu.nus.comp.lms.domain.Settings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FoursquareMapping {

    private final Map<String, String> featureToCategory;
    private final Map<String, String> idToCategory;

    public FoursquareMapping() {
        try (BufferedReader reader = new BufferedReader(new FileReader(Settings.FOURSQUARE_MAPPING))) {
            featureToCategory = new HashMap<>();
            idToCategory = new HashMap<>();

            reader.lines()
                    .map(line -> line.split("\t"))
                    .forEach(arr -> {
                        featureToCategory.put(arr[0], arr[2]);
                        idToCategory.put(arr[1], arr[2]);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't initialize foursquare mapping", e);
        }
    }

    public String getCategoryByFeature(String feature) {
        return featureToCategory.get(feature);
    }

    public String getCategoryById(String id) {
        return idToCategory.get(id);
    }
}
