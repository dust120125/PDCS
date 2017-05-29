package tw.idv.poipoi.pdcs_prototype.fragment;

import android.view.View;

/**
 * Created by DuST on 2017/4/30.
 */

public abstract class ExtendableOnClickListener implements View.OnClickListener {

    private Object[] params;

    public ExtendableOnClickListener(Object... params) {
        this.params = params;
    }

    public Object getParameter(int index){
        return params[index];
    }
}
