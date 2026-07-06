package app.morphe.extension.patches;


import android.util.Log;

public class MapStylePatches {
    public static Enum replaceMapStyle(Enum original) {
        try {
            String style = original.toString();
            String styleName = "ROADMAP";

            if (style.contains("SATELLITE")) {
                styleName = "SATELLITE_HYBRID";
            } else if (style.contains("LOW_LIGHT")) {
                styleName = "NAVIGATION_WALKING_LOW_LIGHT";
            }

            return Enum.valueOf(original.getClass(), styleName);
        } catch (Throwable e) {
            Log.e(original.name(), "replaceMapStyle Error", e);
            return original;
        }
    }
}
