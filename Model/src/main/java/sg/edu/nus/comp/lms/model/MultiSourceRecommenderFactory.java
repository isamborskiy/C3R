package sg.edu.nus.comp.lms.model;

import sg.edu.nus.comp.lms.model.multi.MultiSourceRecommender;
import weka.core.Instances;

import java.util.List;

public interface MultiSourceRecommenderFactory {

    MultiSourceRecommender create(List<Instances> sources);
}
