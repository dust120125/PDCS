package org.dust.capApi;

import android.support.annotation.NonNull;

import org.dust.capApi.parser.AreaFactory;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Info implements Comparable<Info>, Serializable {

    public String capId;
    public String language;
    public ArrayList<String> category = new ArrayList<>();
    public String event;
    public ArrayList<String> responseType = new ArrayList<>();
    public String urgency;
    public String severity;
    public String certainty;
    public String audience;
    public ArrayList<Value> eventCode = new ArrayList<>();
    public String effective;
    public String onset;
    public String expires;
    public String senderName;
    public String headline;
    public String description;
    public String instruction;
    public String web;
    public String contact;
    public ArrayList<Value> parameter = new ArrayList<>();
    public ArrayList<Resource> resource = new ArrayList<>();
    public ArrayList<Area> area = new ArrayList<>();

    private Map<String, Area> commonSeverityArea;
    private transient Severity.SeverityCode actualEffect;

    public AlertStatus getStatus() {
        Date now = new Date();

        try {
            Date effTime = CAP.DATE_FORMAT.parse(effective);
            if (now.getTime() - effTime.getTime() >= 0) {
                Date expTime = CAP.DATE_FORMAT.parse(expires);
                if (now.getTime() - expTime.getTime() >= 0) {
                    return AlertStatus.EXPIRED;
                } else {
                    return AlertStatus.EFFECTIVE;
                }
            } else {
                return AlertStatus.NOT_YET;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return AlertStatus.Unknow;
        }
    }

    /**
     * 取得已經過期了多久時間 (毫秒)     *
     * @return 過期了多久時間 (毫秒)
     */
    public long getExpiredTimePast() {
        try {
            Date date = CAP.DATE_FORMAT.parse(this.expires);
            return System.currentTimeMillis() - date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Severity.SeverityCode updateActualEffect(String geocode) {
        Severity.SeverityCode severity = Severity.SeverityCode.None;
        Map<String, Area> areas = getCommonSeverityArea();
        for (Area a : areas.values()) {
            for (Value v : a.geocode) {
                if (geocode.startsWith(v.value)) {
                    actualEffect = a.severity;
                    return actualEffect;
                } else if (v.value.split("-")[0].equals(geocode.split("-")[0])) {
                    severity = Severity.SeverityCode.SameTown;
                }
            }
        }
        actualEffect = severity;
        return actualEffect;
    }

    public Severity.SeverityCode getActualEffect() {
        return actualEffect;
    }

    public Map<String, Area> getCommonSeverityArea() {
        if (commonSeverityArea == null) {
            commonSeverityArea = AreaFactory.getSeverity(this);
        }
        return commonSeverityArea;
    }

    public static String getUrgency(String urgency) {
        switch (urgency) {
            case "Immediate":
                return "應立即採取應變";
            case "Expected":
                return "應該於一小時內盡快採取應變";
            case "Future":
                return "應採取應變";
            case "Past":
                return "已不須採取應變";
            case "Unknown":
                return "未知";
            default:
                return "未知";
        }
    }

    public static String getCertaintyText(String certainty) {
        switch (certainty) {
            case "Observed":
                return "確定已發生或將發生";
            case "Likely":
                return "超過一半的機率會發生";
            case "Possible":
                return "可能會發生";
            case "Unlikely":
                return "可能不會發生";
            case "Unknown":
                return "未知";
            default:
                return "未知";
        }
    }

    @Override
    public int compareTo(@NonNull Info another) {
        return Severity.compare(this.actualEffect, another.actualEffect) * -1;
    }
}
