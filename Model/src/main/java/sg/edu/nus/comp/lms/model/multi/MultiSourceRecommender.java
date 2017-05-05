package sg.edu.nus.comp.lms.model.multi;

import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import sg.edu.nus.comp.lms.model.Recommender;
import weka.core.Instances;

import java.util.List;

public abstract class MultiSourceRecommender extends Recommender {

    protected final List<Instances> sources;
    protected final List<String> ids;

    public MultiSourceRecommender(List<Instances> sources) {
        this.sources = sources;
        this.ids = InstancesUtils.extractIntersectionStringAttr(sources, Settings.ID_ATTR);
    }
}
