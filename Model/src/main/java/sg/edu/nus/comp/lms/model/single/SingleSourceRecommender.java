package sg.edu.nus.comp.lms.model.single;

import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.Recommender;
import weka.core.Instances;

import java.util.List;

public abstract class SingleSourceRecommender extends Recommender {

    protected final Instances instances;
    protected final List<String> ids;

    public SingleSourceRecommender(Instances instances) {
        this.instances = instances;
        this.ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
    }
}
