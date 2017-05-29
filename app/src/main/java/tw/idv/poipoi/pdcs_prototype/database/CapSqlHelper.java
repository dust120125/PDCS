package tw.idv.poipoi.pdcs_prototype.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


/**
 * Created by DuST on 2017/4/29.
 */

public class CapSqlHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "cap.db";

    public static final int VERSION = 1;

    private static SQLiteDatabase database;

    public CapSqlHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context){
        if (database == null || !database.isOpen()){
            database = new CapSqlHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
