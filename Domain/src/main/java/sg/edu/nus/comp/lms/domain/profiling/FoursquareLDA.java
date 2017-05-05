package sg.edu.nus.comp.lms.domain.profiling;

import com.opencsv.CSVReader;
import sg.edu.nus.comp.lms.domain.Settings;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoursquareLDA {

    private final Map<String, List<String>> topicToCategories;

    public FoursquareLDA() {
        try (CSVReader reader = new CSVReader(new FileReader(Settings.FOURSQUARE_LDA))) {
            reader.readNext(); // skip header
            topicToCategories = new HashMap<>();

            FoursquareMapping mapping = new FoursquareMapping();
            reader.readAll().stream()
                    .forEach(line -> {
                        String topicName = line[1];
                        String category = mapping.getCategoryById(line[2]);
                        List<String> categories = topicToCategories.getOrDefault(topicName, new ArrayList<>());
                        categories.add(category);
                        topicToCategories.put(topicName, categories);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't initialize foursquare lda topics", e);
        }
    }

    public List<String> getCategories(String topicName) {
        return topicToCategories.get(topicName);
    }
}
