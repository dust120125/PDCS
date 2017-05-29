package tw.idv.poipoi.pdcs;

import android.app.Service;
import android.os.Binder;

/**
 * Created by DuST on 2017/5/21.
 */

public abstract class ServiceBinder extends Binder {
    abstract public Service getService();
}
