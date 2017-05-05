package sg.edu.nus.comp.lms.domain.entity;

import java.util.HashMap;
import java.util.Map;

public enum City {

    LONDON,
    NEW_YORK,
    SINGAPORE;

    private static Map<String, City> strToCity = new HashMap<>(City.values().length);

    static {
        strToCity.put("London", LONDON);
        strToCity.put("NewYork", NEW_YORK);
        strToCity.put("Singapore", SINGAPORE);
    }

    public static City fromString(String value) {
        return strToCity.get(value);
    }

    @Override
    public String toString() {
        return strToCity.keySet().stream()
                .filter(key -> strToCity.get(key) == this)
                .findFirst()
                .orElse(null);
    }
}
