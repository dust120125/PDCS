package org.dust.capApi;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by DuST on 2017/4/9.
 */
public class CAP implements Comparable<CAP>, Serializable{

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);

    public String identifier;
    public String sender;
    public String sent;
    public String status;
    public String msgType;
    public String source;
    public String scope;
    public String restriction;
    public String addresses;
    public ArrayList<String> code = new ArrayList<>();
    public String note;
    public String references;
    public String incidents;
    public ArrayList<Info> info = new ArrayList<>();

    private boolean notified = false;
    private transient Severity.SeverityCode actualEffect;

    public AlertStatus getStatus() {
        AlertStatus status = AlertStatus.EXPIRED;
        for (Info i : info) {
            switch (i.getStatus()) {
                case NOT_YET:
                    status = AlertStatus.NOT_YET;
                    break;
                case EFFECTIVE:
                    return AlertStatus.EFFECTIVE;
            }
        }
        return status;
    }

    /**
     * 取得已經過期了多久時間 (毫秒)     *
     * @return 過期了多久時間 (毫秒)
     */
    public long getExpiredTimePast() {
        long time = -1;
        for (Info i : info) {
            long tmp = i.getExpiredTimePast();
            if (time == -1 || time > tmp){
                time = tmp;
            }
        }
        return time;
    }

    public Severity.SeverityCode updateActualEffect(String geocode){
        Severity.SeverityCode severity = Severity.SeverityCode.None;
        for (Info i : info) {
            Severity.SeverityCode tmp = i.updateActualEffect(geocode);
            if (tmp != Severity.SeverityCode.None){
                if (Severity.compare(tmp, severity) == 1) {
                    severity = tmp;
                }
            }
        }
        actualEffect = severity;
        return severity;
    }

    public Severity.SeverityCode getActualEffect() {
        return actualEffect;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    @Override
    public int compareTo(@NonNull CAP another) {
        AlertStatus status = this.getStatus();
        AlertStatus status1 = another.getStatus();
        if (status == status1) {
            return Severity.compare(this.actualEffect, another.actualEffect) * -1;
        }

        if (status == AlertStatus.EFFECTIVE) return -1;
        if (status == AlertStatus.EXPIRED || status1 == AlertStatus.EFFECTIVE){
            return 1;
        } else {
            return -1;
        }
    }
}
