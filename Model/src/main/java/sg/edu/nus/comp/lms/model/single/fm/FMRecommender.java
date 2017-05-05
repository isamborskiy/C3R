package sg.edu.nus.comp.lms.model.single.fm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FMRecommender {

    private static final String LIBFM_PATH = "../bin/libFM";

    private final Transformation transformation;
    private final int nFactors;
    private final int iterationNumber;

    private String trainFilePath;
    private String testFilePath;
    private String predictionFilePath;

    public FMRecommender(Transformation transformation, int nFactors, int iterationNumber) {
        this.transformation = transformation;
        this.nFactors = nFactors;
        this.iterationNumber = iterationNumber;
    }

    private void setTrainPath(String s) {
        this.trainFilePath = s;
    }

    private void setTestPath(String s) {
        this.testFilePath = s;
    }

    private void setPredictionPath(String s) {
        this.predictionFilePath = s;
    }

    public List<Double> run() throws IOException {
        List<String> args = Arrays.asList(
                LIBFM_PATH,
                "-task", "r",
                "-train", trainFilePath,
                "-test", testFilePath,
                "-dim", "'1,1," + nFactors + "'",
                "-iter", String.valueOf(iterationNumber),
                "-out", predictionFilePath
        );

        // run libFM
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        try (BufferedReader stdout = new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
            String line;
            while ((line = stdout.readLine()) != null) {
                if (line.contains("ERROR: unable to open")) {
                    throw new IOException("File IO Error. Please check if the file paths are valid.");
                } else if (line.contains("ERROR")) {
                    throw new RuntimeException("Caught error from libFM. Please check the model parameters.");
                }
            }

            // parse prediction file, return the result
            Path p = FileSystems.getDefault().getPath(predictionFilePath);
            return parsePredictionFile(p);
        }
    }

    private List<Double> parsePredictionFile(Path p) throws IOException {
        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        return lines.stream()
                .map(line -> Double.parseDouble(line.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Get the top-recommendationSize recommendation item list for a given user.
     * The returned list will only contain the unseen items for the user.
     */
    public int[] getRecommendationList(String user) throws IOException {
        List<String> trainLines = transformation.toLibfmFormat();
        List<String> testLines = new ArrayList<>();
        for (Integer itemId : transformation.getItemIds()) {
            int userId = transformation.convertUser(user);
            itemId = transformation.convertItem(itemId);
            double rating = 0./*transformation.getRating(userId, itemId)*/;
//            if (rating == 0.) {
            testLines.add(transformation.toLibfmFormat(userId, itemId, rating));
//            }
        }

        // initialize the temporary files
        Path trainPath = Files.createTempFile(null, null);
        Path testPath = Files.createTempFile(null, null);
        Path predictionPath = Files.createTempFile(null, null);
        Files.write(trainPath, trainLines, StandardCharsets.UTF_8);
        Files.write(testPath, testLines, StandardCharsets.UTF_8);

        // set file path and run recommendation
        setTrainPath(trainPath.toString());
        setTestPath(testPath.toString());
        setPredictionPath(predictionPath.toString());
        List<Double> predictions = run();

        // delete the temporary files
        Files.delete(trainPath);
        Files.delete(testPath);
        Files.delete(predictionPath);

        // sort the items according to the predicted scores
        return IntStream.range(0, testLines.size())
                .boxed()
                .sorted((o1, o2) -> -Double.compare(predictions.get(o1), predictions.get(o2)))
                .mapToInt(i -> i)
                .toArray();
    }
}
