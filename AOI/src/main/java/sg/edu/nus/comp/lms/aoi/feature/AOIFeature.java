package sg.edu.nus.comp.lms.aoi.feature;

import sg.edu.nus.comp.lms.aoi.entity.UserAOI;

public interface AOIFeature {

    String getValue(UserAOI userAOI);

    String getName();
}
