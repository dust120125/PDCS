package tw.idv.poipoi.pdcs.alert;

import org.dust.capApi.CAP;
import org.dust.capApi.Info;
import org.dust.util.LruCache;

import java.util.Map;

/**
 * Created by DuST on 2017/6/1.
 */

public class AlertFactory {

    private static LruCache<String, AlertParser> parserLruCache;
    private static AlertParser defalutParser = new AP_defalut();

    static {
        parserLruCache = new LruCache<>(5);
    }

    public static boolean needAlert(CAP cap) {
        for (Info info : cap.info) {
            if (needAlert(info)){
                return true;
            }
        }
        return false;
    }

    public static boolean needAlert(Info info){
        AlertParser ap;
        String eventCode = info.eventCode.get(0).value;
        String className = "AP_" + eventCode;

        if ((ap = parserLruCache.get(className)) == null) {
            try {
                Class parserClass = Class.forName("tw.idv.poipoi.pdcs.alert" + className);
                ap = (AlertParser) parserClass.newInstance();
                parserLruCache.put(className, ap);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                ap = defalutParser;
            }
        }

        return ap.parse(info);
    }

    public static void cleanCache(){
        parserLruCache.clear();
    }

}
