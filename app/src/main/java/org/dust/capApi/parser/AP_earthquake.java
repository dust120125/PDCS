/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dust.capApi.parser;

import org.dust.capApi.Area;
import org.dust.capApi.Info;
import org.dust.capApi.Value;

import org.dust.capApi.Severity.SeverityCode;
import static org.dust.capApi.Severity.SeverityCode.Extreme;
import static org.dust.capApi.Severity.SeverityCode.Minor;
import static org.dust.capApi.Severity.SeverityCode.Moderate;
import static org.dust.capApi.Severity.SeverityCode.Severe;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DuST
 */
public class AP_earthquake extends AreaParser {

    private final SeverityCode[] INTENSITY = {
        Minor, Minor, Minor, Minor, Minor, Moderate, Severe, Extreme, Extreme, Extreme, Extreme
    };

    @Override
    public Map<String, Area> parse(Info info) {
        SeverityCode infoSeverity = SeverityCode.valueOf(info.severity);
        for (Value v : info.parameter) {
            if (v.valueName.equals("LocalMaxIntensity")) {
                String[] paras = v.value.split(";");

                int intensity = Character.getNumericValue(paras[0].charAt(0));
                String geocode = paras[3];
                SeverityCode severity = INTENSITY[intensity];

                addArea(severity, new Value("magnitude=" + intensity, geocode));
            }
        }
        for (Area area : info.area) {
            for (String s : area.circle) {
                addCircle(infoSeverity, area.areaDesc, s);
            }
        }
        return getAreas();
    }

}
