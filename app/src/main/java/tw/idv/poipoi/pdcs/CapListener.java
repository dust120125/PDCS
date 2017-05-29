package tw.idv.poipoi.pdcs;

import java.util.ArrayList;

import org.dust.capApi.CAP;

/**
 * Created by DuST on 2017/5/18.
 */

public interface CapListener {

    void onCapChanged(ArrayList<CAP> caps);

}
