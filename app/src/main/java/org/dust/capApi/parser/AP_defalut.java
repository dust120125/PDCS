package org.dust.capApi.parser;

import org.dust.capApi.Area;
import org.dust.capApi.Info;
import org.dust.capApi.Severity;
import org.dust.capApi.Value;

import java.util.Map;

/**
 * Created by DuST on 2017/5/22.
 */

public class AP_defalut extends AreaParser {

    public static final String NAME = "AP_defalut";

    @Override
    public Map<String, Area> parse(Info info) {
        Severity.SeverityCode severity = Severity.SeverityCode.valueOf(info.severity);
        for(int i = 0; i < info.area.size(); i++){
            Area area = info.area.get(i);
            area.severity = severity;
            getAreas().put(String.valueOf(i), area);
        }
        /*
        for(Area a : info.area){
            for(Value v : a.geocode){
                addArea(severity, v);
            }
            for (String s : a.circle) {
                addCircle(severity, a.areaDesc, s);
            }
        }
        */
        return getAreas();
    }
}
