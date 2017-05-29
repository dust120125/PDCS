package tw.idv.poipoi.pdcs;

import android.graphics.Color;

import org.dust.capApi.Severity;

/**
 * Created by DuST on 2017/5/26.
 */

public class SeverityColor {

    public static final int SEVERITY_COLOR_MINOR;
    public static final int SEVERITY_COLOR_MODERATE;
    public static final int SEVERITY_COLOR_SEVERE;
    public static final int SEVERITY_COLOR_EXTREME;

    public static final int SEVERITY_TEXT_COLOR_MINOR;
    public static final int SEVERITY_TEXT_COLOR_MODERATE;
    public static final int SEVERITY_TEXT_COLOR_SEVERE;
    public static final int SEVERITY_TEXT_COLOR_EXTREME;

    static {
        SEVERITY_COLOR_MINOR = Color.parseColor("#80" + Severity.COLOR_MINOR.substring(1));
        SEVERITY_COLOR_MODERATE = Color.parseColor("#80" + Severity.COLOR_MODERATE.substring(1));
        SEVERITY_COLOR_SEVERE = Color.parseColor("#80" + Severity.COLOR_SEVERE.substring(1));
        SEVERITY_COLOR_EXTREME = Color.parseColor("#80" + Severity.COLOR_EXTREME.substring(1));

        SEVERITY_TEXT_COLOR_MINOR = Color.parseColor(Severity.COLOR_MINOR);
        SEVERITY_TEXT_COLOR_MODERATE = Color.parseColor(Severity.COLOR_MODERATE);
        SEVERITY_TEXT_COLOR_SEVERE = Color.parseColor(Severity.COLOR_SEVERE);
        SEVERITY_TEXT_COLOR_EXTREME = Color.parseColor(Severity.COLOR_EXTREME);
    }

    public static int getSeverityColor(String severity) {
        switch (severity) {
            case "Minor":
                return SEVERITY_COLOR_MINOR;
            case "Moderate":
                return SEVERITY_COLOR_MODERATE;
            case "Severe":
                return SEVERITY_COLOR_SEVERE;
            case "Extreme":
                return SEVERITY_COLOR_EXTREME;
            default:
                return SEVERITY_COLOR_MODERATE;
        }
    }

    public static int getSeverityColor(Severity.SeverityCode severity) {
        switch (severity) {
            case Minor:
                return SEVERITY_COLOR_MINOR;
            case Moderate:
                return SEVERITY_COLOR_MODERATE;
            case Severe:
                return SEVERITY_COLOR_SEVERE;
            case Extreme:
                return SEVERITY_COLOR_EXTREME;
            default:
                return SEVERITY_COLOR_MODERATE;
        }
    }

    public static int getSeverityTextColor(String severity) {
        switch (severity) {
            case "Minor":
                return SEVERITY_TEXT_COLOR_MINOR;
            case "Moderate":
                return SEVERITY_TEXT_COLOR_MODERATE;
            case "Severe":
                return SEVERITY_TEXT_COLOR_SEVERE;
            case "Extreme":
                return SEVERITY_TEXT_COLOR_EXTREME;
            default:
                return SEVERITY_TEXT_COLOR_MODERATE;
        }
    }

    public static int getSeverityTextColor(Severity.SeverityCode severity) {
        switch (severity) {
            case Minor:
                return SEVERITY_TEXT_COLOR_MINOR;
            case Moderate:
                return SEVERITY_TEXT_COLOR_MODERATE;
            case Severe:
                return SEVERITY_TEXT_COLOR_SEVERE;
            case Extreme:
                return SEVERITY_TEXT_COLOR_EXTREME;
            default:
                return SEVERITY_TEXT_COLOR_MODERATE;
        }
    }

}
