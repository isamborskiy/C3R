package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.model.RecommenderFactory;
import weka.core.Instances;

public class CFSingleLayer extends CFRecommender {

    public CFSingleLayer(Instances instances) {
        super(instances);
    }

    public static class CFSingleLayerFactory implements RecommenderFactory {

        @Override
        public CFSingleLayer create(Instances instances) {
            return new CFSingleLayer(instances);
        }
    }
}
