package tw.idv.poipoi.pdcs.database;

/**
 * Created by DuST on 2017/5/31.
 */

public interface OnDataChangedListener<T> {

    void onInsert(T data);

    void onUpdate(T data);

    void onDelete(T data);

}
