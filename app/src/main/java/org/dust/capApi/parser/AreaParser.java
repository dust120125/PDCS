/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dust.capApi.parser;

import org.dust.capApi.Area;
import org.dust.capApi.Info;
import org.dust.capApi.Severity;
import org.dust.capApi.Value;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DuST
 */
public abstract class AreaParser {

    private HashMap<String, Area> areas = new HashMap<>();
    
    abstract public Map<String, Area> parse(Info info);

    protected final void addArea(Severity.SeverityCode severityCode, Value value){
        getArea(severityCode.name(), severityCode).geocode.add(value);
    }

    protected final void addCircle(Severity.SeverityCode severityCode, String desc, String circle){
        getArea(desc, severityCode).circle.add(circle);
    }

    private final Area getArea(String key, Severity.SeverityCode severityCode){
        Area area;
        if (!areas.containsKey(key)){
            area = new Area(key);
            area.severity = severityCode;
            areas.put(key, area);
        } else {
            area = areas.get(key);
        }
        return area;
    }

    public final HashMap<String, Area> getAreas(){
        return areas;
    }
    
}
