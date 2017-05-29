package org.dust.capApi;

import java.io.Serializable;
import java.util.ArrayList;

public class Area implements Serializable {

    public String areaDesc;
    public ArrayList<String> polygon = new ArrayList<>();
    public ArrayList<String> circle = new ArrayList<>();
    public ArrayList<Value> geocode = new ArrayList<>();
    public int altitude;
    public int ceiling;

    public Severity.SeverityCode severity;

    public Area(String areaDesc) {
        this.areaDesc = areaDesc;
    }
}
