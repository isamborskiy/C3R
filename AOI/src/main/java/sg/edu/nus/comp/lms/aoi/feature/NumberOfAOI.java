package sg.edu.nus.comp.lms.aoi.feature;

import sg.edu.nus.comp.lms.aoi.entity.UserAOI;

public class NumberOfAOI implements AOIFeature {

    @Override
    public String getValue(UserAOI userAOI) {
        return String.valueOf(userAOI.getPoints().size());
    }

    @Override
    public String getName() {
        return "number_of_aoi";
    }
}
