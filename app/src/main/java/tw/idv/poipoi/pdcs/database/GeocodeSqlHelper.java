package tw.idv.poipoi.pdcs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by DuST on 2017/5/22.
 */

public class GeocodeSqlHelper extends SQLiteOpenHelper {

    private static SQLiteDatabase database;

    public static final int VERSION = 1;
    public static final String DATABASE_NAME = "GeoCode.db";

    public GeocodeSqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new GeocodeSqlHelper(context, DATABASE_NAME,
                    null, VERSION).getWritableDatabase();
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
