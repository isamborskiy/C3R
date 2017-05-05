package sg.edu.nus.comp.lms.layer;

import com.opencsv.CSVReader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.util.ArraysUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TemporalLayerBuilder {

    private static final String ID = "_id";
    private static final String TIME = "time_utc";

    private static final int INTERVAL = 3;
    private static final int NUMBER_OF_INTERVAL = (int) (TimeUnit.DAYS.toHours(1) / INTERVAL);

    private static final double[] EMPTY_PATTERN = new double[NUMBER_OF_INTERVAL];

    private static final String DAYS = "days_";
    private static final String WEEKENDS = "weekends_";

    private static final String TWITTER_FEATURE = "t%d_%d";
    private static final String FOURSQUARE_FEATURE = "f%d_%d";
    private static final String INSTAGRAM_FEATURE = "i%d_%d";

    public static void main(String[] args) throws IOException {
        Map<String, double[]> twitter = extractUsers(Settings.VORONTSOV_TWEETS);
        Map<String, double[]> foursquare = extractUsers(Settings.VORONTSOV_CHECKINS);
        Map<String, double[]> instagram = extractUsers(Settings.VORONTSOV_IMAGES);

        removeExtraIds(twitter, foursquare);
        removeExtraIds(twitter, instagram);
        removeExtraIds(instagram, foursquare);

        try (PrintWriter writer = new PrintWriter("temporalPattern" + Settings.CITY_NAME + ".csv")) {
            writer.print(Settings.ID_ATTR);
            writeHeader(writer, DAYS + TWITTER_FEATURE, "");
            writeHeader(writer, WEEKENDS + TWITTER_FEATURE, "");
            writeHeader(writer, DAYS + FOURSQUARE_FEATURE, "");
            writeHeader(writer, WEEKENDS + FOURSQUARE_FEATURE, "");
            writeHeader(writer, DAYS + INSTAGRAM_FEATURE, "");
            writeHeader(writer, WEEKENDS + INSTAGRAM_FEATURE, "\n");

            for (String id : twitter.keySet()) {
                writer.print(id);
                writeValues(writer, twitter.get(id), "");
                writeValues(writer, foursquare.getOrDefault(id, EMPTY_PATTERN), "");
                writeValues(writer, instagram.getOrDefault(id, EMPTY_PATTERN), "\n");
            }
        }
    }

    private static void writeValues(PrintWriter writer, double[] values, String suffix) {
        writer.print(Arrays.stream(values)
                .mapToObj(value -> String.format("%.3f", value))
                .collect(Collectors.joining(",", ",", suffix)));
    }

    private static void writeHeader(PrintWriter writer, String pattern, String suffix) {
        writer.print(IntStream.range(0, NUMBER_OF_INTERVAL)
                .mapToObj(i -> String.format(pattern, i * INTERVAL, (i + 1) * INTERVAL))
                .collect(Collectors.joining(",", ",", suffix)));
    }

    private static void removeExtraIds(Map<String, double[]> map1, Map<String, double[]> map2) {
        map1.entrySet().removeIf(entry -> !map2.containsKey(entry.getKey()));
        map2.entrySet().removeIf(entry -> !map1.containsKey(entry.getKey()));
    }

    private static Map<String, double[]> extractUsers(String filename) throws IOException {
        Map<String, double[]> usersActivity = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] header = reader.readNext(); // skip header
            int idIndex = ArraysUtils.indexOf(header, ID);
            int timeIndex = ArraysUtils.indexOf(header, TIME);

            String[] line;
            while ((line = reader.readNext()) != null) {
                String id = line[idIndex];
                DateTime date = new DateTime(Long.parseLong(line[timeIndex]));
                int partOfDay = (date.getHourOfDay() / INTERVAL) + (isWeekend(date) ? NUMBER_OF_INTERVAL : 0);

                // weekdays + weekends
                double[] hourDistribution = usersActivity.getOrDefault(id, new double[2 * NUMBER_OF_INTERVAL]);
                hourDistribution[partOfDay]++;
                usersActivity.put(id, hourDistribution);
            }

            for (String id : usersActivity.keySet()) {
                double[] hourDistribution = usersActivity.get(id);
                double amount = Arrays.stream(hourDistribution).sum();
                for (int i = 0; i < hourDistribution.length; i++) {
                    hourDistribution[i] /= amount;
                }
            }
        }
        return usersActivity;
    }

    private static boolean isWeekend(DateTime date) {
        return date.dayOfWeek().get() == DateTimeConstants.SATURDAY
                || date.dayOfWeek().get() == DateTimeConstants.SUNDAY;
    }
}
