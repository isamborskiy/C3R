package sg.edu.nus.comp.lms.domain.profiling;

import com.opencsv.CSVReader;
import sg.edu.nus.comp.lms.domain.Settings;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitterLDA {

    private final Map<String, List<String>> topicToWords;

    public TwitterLDA() {
        try (CSVReader reader = new CSVReader(new FileReader(Settings.TWITTER_LDA))) {
            reader.readNext(); // skip header
            topicToWords = new HashMap<>();

            reader.readAll().stream()
                    .forEach(line -> {
                        String topicName = line[0];
                        String word = line[1];
                        List<String> words = topicToWords.getOrDefault(topicName, new ArrayList<>());
                        words.add(word);
                        topicToWords.put(topicName, words);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Couldn't initialize twitter lda topics", e);
        }
    }

    public List<String> getWords(String topicName) {
        return topicToWords.get(topicName);
    }
}
