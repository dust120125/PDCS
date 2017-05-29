/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dust.capApi.parser;

import org.dust.capApi.Area;
import org.dust.capApi.Info;
import org.dust.capApi.Severity.SeverityCode;
import org.dust.capApi.parser.AP_defalut;
import org.dust.capApi.parser.AreaParser;
import java.util.Map;

/**
 *
 * @author DuST
 */
public class AreaFactory {

    //可以加入 LruCache 做優化

    public static Map<String, Area> getSeverity(Info info) {
        AreaParser ap;
        String eventCode = info.eventCode.get(0).value;
        String className = "AP_" + eventCode;

        try {
            Class parserClass = Class.forName("org.dust.capApi.parser." + className);
            ap = (AreaParser) parserClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ap = new AP_defalut();
        }

        return ap.parse(info);
    }

}
