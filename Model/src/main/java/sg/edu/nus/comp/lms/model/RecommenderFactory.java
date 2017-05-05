package sg.edu.nus.comp.lms.model;

import weka.core.Instances;

public interface RecommenderFactory {

    Recommender create(Instances instances);
}
