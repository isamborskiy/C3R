package sg.edu.nus.comp.lms.domain.reader;

import sg.edu.nus.comp.lms.domain.weka.operation.IntersectInstances;
import sg.edu.nus.comp.lms.domain.weka.operation.MergeInstances;
import weka.core.Instances;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DataReader {

    protected static final String USER_ID_ATTR_NAME = "_id";

    protected final File[] files;

    public DataReader(File file) {
        files = new File[]{file};
    }

    public DataReader(File... files) {
        this.files = files;
    }

    public Instances readAll() {
        return join(Arrays.stream(files)
                .map(this::read)
                .collect(Collectors.toList()));
    }

    protected abstract Instances read(File file);

    private Instances join(List<Instances> instancesList) {
        Instances instances = instancesList.get(0);
        instances.attribute(USER_ID_ATTR_NAME).setWeight(0.);
        for (int i = 1; i < instancesList.size(); i++) {
            instances = join(instances, instancesList.get(i));
        }
        return instances;
    }

    private Instances join(Instances first, Instances second) {
        new IntersectInstances(first, second).eval();
        return new MergeInstances(first, second).eval();
    }
}
