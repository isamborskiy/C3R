package sg.edu.nus.comp.lms.domain.profiling;

import com.opencsv.CSVReader;
import sg.edu.nus.comp.lms.domain.Settings;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class InstagramMapping {

    private final Map<String, String> featureToCategory;

    public InstagramMapping() {
        try (CSVReader reader = new CSVReader(new FileReader(Settings.INSTAGRAM_MAPPING))) {
            reader.readNext(); // skip header
            featureToCategory = reader.readAll().stream()
                    .collect(Collectors.toMap(
                            arr -> arr[0],
                            arr -> arr[1]
                    ));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't initialize instagram mapping", e);
        }
    }

    public String getCategory(String feature) {
        return featureToCategory.get(feature);
    }
}
