/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dust.capApi.parser;

import android.util.Log;

import org.dust.capApi.Area;
import org.dust.capApi.Info;
import org.dust.capApi.Severity.SeverityCode;
import org.dust.capApi.parser.AP_defalut;
import org.dust.capApi.parser.AreaParser;
import org.dust.util.LruCache;

import java.util.Map;

/**
 *
 * @author DuST
 */
public class AreaFactory {

    //可以加入 LruCache 做優化

    private static LruCache<String, Class> parserLruCache;

    static {
        parserLruCache = new LruCache<>(5);
    }

    public static Map<String, Area> getSeverity(Info info) {
        AreaParser ap;
        String eventCode = info.eventCode.get(0).value;
        String className = "AP_" + eventCode;

        Class parserClass;
        if ((parserClass = parserLruCache.get(className)) == null) {
            try {
                parserClass = Class.forName("org.dust.capApi.parser." + className);
                parserLruCache.put(className, parserClass);
            } catch (ClassNotFoundException e) {
                Log.d("AreaFactory", className + " No Found");
            }
        }

        if (parserClass != null) {
            try {
                ap = (AreaParser) parserClass.newInstance();
                return ap.parse(info);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Log.d("AreaFactory", "Use AP_defalut to parse");
        return new AP_defalut().parse(info);
    }

    public static void cleanCache(){
        parserLruCache.clear();
    }

}
