package tw.idv.poipoi.pdcs.user;

import android.location.Location;

import org.dust.capApi.Severity;

import java.util.Date;

import tw.idv.poipoi.pdcs.Core;
import tw.idv.poipoi.pdcs.Setting;

/**
 * Created by DuST on 2017/12/4.
 */

public class UserStatus {

    public static final String SAFETY_STATUS_SAFE = "safe";
    public static final String SAFETY_STATUS_UNKNOW = "unknow";
    public static final String SAFETY_STATUS_DANGER = "danger";

    String safety;
    Location location;
    Severity.SeverityCode severity;
    String event;

    public String getSafety() {
        return safety;
    }

    public void setSafety(String safety) {
        this.safety = safety;
        User.getInstance().serverService(Setting.SERVER_DOMAIN + "android_login/UpdateUserStatus.php?mode=safety",
                new String[]{
                        "safety=" + safety
                }
        );
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        User.getInstance().serverService(Setting.SERVER_DOMAIN + "android_login/UpdateLocation.php",
                new String[]{
                        "time=" + Core.dateFormat.format(new Date()),
                        "latitude=" + location.getLatitude(),
                        "longitude=" + location.getLongitude()
                }
        );
    }

    public Severity.SeverityCode getSeverity() {
        return severity;
    }

    public void setSeverity(Severity.SeverityCode severity) {
        this.severity = severity;
        User.getInstance().serverService(Setting.SERVER_DOMAIN + "android_login/UpdateUserStatus.php?mode=severity",
                new String[]{
                        "severity=" + severity.name()
                }
        );
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
        if (event == null || event.isEmpty()) {
            event = "unknow";
        }

        User.getInstance().serverService(Setting.SERVER_DOMAIN + "android_login/UpdateUserStatus.php?mode=event",
                new String[]{
                        "event=" + event,
                        "latitude=" + location.getLatitude(),
                        "longitude=" + location.getLongitude()
                }
        );
    }
}
