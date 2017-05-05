package sg.edu.nus.comp.lms.domain.reader;

import sg.edu.nus.comp.lms.domain.entity.SocialNetwork;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class CSVReader extends DataReader {

    public CSVReader(File file) {
        super(file);
    }

    public CSVReader(File... files) {
        super(files);
    }

    public CSVReader(SocialNetwork socialNetwork, File directory) {
        this(socialNetwork.findFiles(directory)
                .toArray(new File[socialNetwork.getFiles().length]));
    }

    @Override
    protected Instances read(File file) {
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(file);
            return loader.getDataSet();
        } catch (IOException e) {
            throw new IllegalStateException("Incorrect file format or file path: " + file.getName(), e);
        }
    }
}
