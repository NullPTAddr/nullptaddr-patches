package app.morphe.extension.maps.patches;


import android.util.Log;

public class MapStylePatches {
    public static Enum replaceMapStyle(Enum original) {
        try {
            String style = original.toString();
            String styleName = style;

            if (style.contains("SATELLITE")) {
                styleName = "SATELLITE_HYBRID";
            } else if (style.contains("LOW_LIGHT")) {
                styleName = "NAVIGATION_WALKING_LOW_LIGHT";
            }
            return Enum.valueOf(original.getClass(), styleName);
        } catch (Throwable e) {
            Log.e("MapStylePatches", "replaceMapStyle Error", e);
            return original;
        }
    }
}
