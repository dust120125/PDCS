package tw.idv.poipoi.pdcs.alert;

import org.dust.capApi.Info;

import tw.idv.poipoi.pdcs.CareService;

/**
 * Created by DuST on 2017/6/1.
 */

public class AP_defalut implements AlertParser{

    @Override
    public boolean parse(Info info) {
        if (info.getActualEffect().compareTo(CareService.ALERT_SEVERITY) >= 0){
            return true;
        }
        return false;
    }

}
