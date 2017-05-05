package sg.edu.nus.comp.lms.aoi.feature;

import sg.edu.nus.comp.lms.aoi.entity.UserAOI;

public class Id implements AOIFeature {

    @Override
    public String getValue(UserAOI userAOI) {
        return userAOI.getId();
    }

    @Override
    public String getName() {
        return "_id";
    }
}
