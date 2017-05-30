package tw.idv.poipoi.pdcs.user;

/**
 * Created by DuST on 2017/5/29.
 */

public class Response {

    private int action;
    private Object[] data;

    public int getAction() {
        return action;
    }

    public Object getData(int index) {
        return data[index];
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setData(Object... data) {
        this.data = data;
    }
}
