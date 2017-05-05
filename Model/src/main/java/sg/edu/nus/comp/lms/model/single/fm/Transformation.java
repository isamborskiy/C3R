package sg.edu.nus.comp.lms.model.single.fm;

import sg.edu.nus.comp.lms.model.single.Popular;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Transformation {

    private final Map<Integer, double[]> data = new HashMap<>();
    private final int[] itemsRank;

    private final Map<Integer, String> idMap = new HashMap<>();
    private final Map<String, Integer> idReverseMap = new HashMap<>();

    private final int itemNumber;
    private final double negative;

    public Transformation(Map<String, double[]> data, double negative) {
        int userId = 0;
        this.itemNumber = data.values().stream().findFirst().get().length;
        for (String user : data.keySet()) {
            double[] values = data.get(user);
            this.data.put(userId, values);
            this.idMap.put(userId, user);
            this.idReverseMap.put(user, userId);
            userId++;
        }
        Popular popular = new Popular(null);
        popular.train(data);
        this.itemsRank = popular.recommend(null);
        this.negative = negative;
    }

    public void writeOutputFile(String outputFilePath) throws IOException {
        Files.write(FileSystems.getDefault().getPath(outputFilePath), toLibfmFormat(), StandardCharsets.UTF_8);
    }

    public List<String> toLibfmFormat() {
        List<String> lines = new ArrayList<>();
        for (Integer userId : data.keySet()) {
            double[] rating = data.get(userId);
            Set<Integer> usedItems = new HashSet<>();
            for (int itemId = 0; itemId < rating.length; itemId++) {
                if (rating[itemId] != 0.) {
                    lines.add(toLibfmFormat(userId, itemId, rating[itemId]));
                    usedItems.add(itemId);
                }
            }
            Arrays.stream(itemsRank)
                    .filter(itemId -> !usedItems.contains(itemId))
                    .limit((long) (usedItems.size() * negative))
                    .forEach(itemId -> lines.add(toLibfmFormat(userId, itemId, rating[itemId])));
        }
        return lines;
    }

    public String toLibfmFormat(int userId, int itemId, double rating) {
//        if (rating != 0.) {
        return String.format("%f %d:1 %d:1", rating, userId, data.size() + itemId);
//        } else {
//            return String.format("0 %d:1 %d:1", userId, data.size() + itemId);
//        }
    }

    public String convertUser(int id) {
        return idMap.get(id);
    }

    public int convertUser(String id) {
        return idReverseMap.get(id);
    }

    public int convertItem(int id) {
        return id - data.size();
    }

    public Set<Integer> getUserIds() {
        return idMap.keySet();
    }

    public List<Integer> getItemIds() {
        return IntStream.range(idMap.size(), idMap.size() + itemNumber).boxed().collect(Collectors.toList());
    }

    public double getRating(int userId, int itemId) {
        return data.get(userId)[convertItem(itemId)];
    }
}