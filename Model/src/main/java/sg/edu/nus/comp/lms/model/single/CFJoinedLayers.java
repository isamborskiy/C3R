package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.RecommenderFactory;
import weka.core.Instances;

import java.util.List;

public class CFJoinedLayers extends CFRecommender {

    public CFJoinedLayers(List<Instances> sources) {
        super(InstancesUtils.joinInstances(sources));
    }

    public static class CFJoinedLayersFactory implements RecommenderFactory {

        public CFJoinedLayers create(List<Instances> sources) {
            return new CFJoinedLayers(sources);
        }

        @Override
        public CFJoinedLayers create(Instances instances) {
            throw new RuntimeException("This method not implemented for this factory");
        }
    }
}
