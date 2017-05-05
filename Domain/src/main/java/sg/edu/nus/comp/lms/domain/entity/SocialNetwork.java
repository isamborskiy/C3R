package sg.edu.nus.comp.lms.domain.entity;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SocialNetwork {

    TWITTER("LDA50Features.csv", "LIWCFeatures.csv", "manuallyDefinedTextFeatures.csv"),
    FOURSQUARE("venueCategoriesLDA6Features.csv", "venueCategoriesFeatures3MonthsTrain.csv"),
    INSTAGRAM("imageConceptsFeatures.csv");

    private final String[] files;

    SocialNetwork(String... files) {
        this.files = files;
    }

    public String[] getFiles() {
        return files;
    }

    public List<File> findFiles(File directory) {
        return Arrays.stream(files)
                .map(fileName -> findFileInDir(directory, fileName))
                .collect(Collectors.toList());
    }

    private File findFileInDir(File directory, String fileName) {
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            File foundFile = file;
            if (file.isDirectory()) {
                foundFile = findFileInDir(file, fileName);
            }
            if (foundFile != null && foundFile.getName().equals(fileName)) {
                return foundFile;
            }
        }
        return null;
    }
}
