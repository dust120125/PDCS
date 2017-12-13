package org.dust.capApi;

/**
 * Created by DuST on 2017/4/30.
 */

public class Severity {

    public enum SeverityCode {

        /**
         * 未知
         */
        Unknown,

        /**
         * 沒有威脅
         */
        None,

        /**
         * 很小的威脅
         */
        Minor,

        /**
         * 有威脅
         */
        Moderate,

        /**
         * 嚴重的威脅
         */
        Severe,

        /**
         * 非常嚴重的威脅
         */
        Extreme,

        /**
         * 同一鄉/鎮/區，不同村/里的災害
         */
        SameTown
    }

    public static final String SEVERITY_UNKNOWN = "未知";
    public static final String SEVERITY_MINOR = "很小的威脅";
    public static final String SEVERITY_MODERATE = "有威脅";
    public static final String SEVERITY_SEVERE = "嚴重的威脅";
    public static final String SEVERITY_EXTREME = "非常嚴重的威脅";

    public static final String COLOR_NOT_EFFECT = "#d6d6d6";

    public static final String COLOR_MINOR = "#34ed06";
    public static final String COLOR_MODERATE = "#fffc00";
    public static final String COLOR_SEVERE = "#ffae00";
    public static final String COLOR_EXTREME = "#ff0000";

    public static String getSeverityText(SeverityCode severity){
        switch (severity){
            case Unknown: return SEVERITY_UNKNOWN;
            case Minor: return SEVERITY_MINOR;
            case Moderate: return SEVERITY_MODERATE;
            case Severe: return SEVERITY_SEVERE;
            case Extreme: return SEVERITY_EXTREME;
            default: return SEVERITY_UNKNOWN;
        }
    }

    public static String getSeverityText(String severity){
        switch (severity){
            case "Unknown": return SEVERITY_UNKNOWN;
            case "Minor": return SEVERITY_MINOR;
            case "Moderate": return SEVERITY_MODERATE;
            case "Severe": return SEVERITY_SEVERE;
            case "Extreme": return SEVERITY_EXTREME;
            default: return SEVERITY_UNKNOWN;
        }
    }

    /**
     * @return -1 => code1 lower,
     * 0 => same,
     * 1 => code1 higher,
     */
    public static int compare(SeverityCode code1, SeverityCode code2) {
        if (code1 == code2) return 0;
        if (code1 == null) return -1;
        if (code2 == null) return 1;
        if (code1 == SeverityCode.None) return -1;
        if (code2 == SeverityCode.None) return 1;

        switch (code1) {
            case Unknown:
                return -1;

            case Minor:
                switch (code2) {
                    case Unknown:
                    case SameTown:
                        return 1;

                    default:
                        return -1;
                }

            case Moderate:
                switch (code2) {
                    case Unknown:
                    case SameTown:
                    case Minor:
                        return 1;

                    default:
                        return -1;
                }

            case Severe:
                switch (code2) {
                    case Unknown:
                    case SameTown:
                    case Minor:
                    case Moderate:
                        return 1;

                    default:
                        return -1;
                }

            case Extreme:
                return 1;

            case SameTown:
                if (code2 == SeverityCode.Unknown) {
                    return 1;
                } else {
                    return -1;
                }

            default:
                return 0;
        }
    }

}
