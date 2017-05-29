package tw.idv.poipoi.pdcs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import tw.idv.poipoi.pdcs.Core;
import tw.idv.poipoi.pdcs.geo.GeoData;
import tw.idv.poipoi.pdcs.geo.Polygon;

/**
 * Created by DuST on 2017/5/3.
 */

public class GeoSql {

    private SQLiteDatabase db;

    static final String TABLE_NAME = "geodata";

    private static final String NAME = "name";
    private static final String D_NAME = "d_name";
    private static final String E_NAME = "e_name";
    private static final String CODE = "code";

    private static final String NAME_103 = "name_103";
    private static final String D_NAME_103 = "d_name_103";
    private static final String E_NAME_103 = "e_name_103";
    private static final String CODE_103 = "code_103";

    private static final String POLYGONS = "polygons";

    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    CODE_103 + " TEXT PRIMARY KEY, " +
                    NAME_103 + " TEXT, " +
                    D_NAME_103 + " TEXT, " +
                    E_NAME_103 + " TEXT, " +
                    CODE + " TEXT, " +
                    NAME + " TEXT, " +
                    D_NAME + " TEXT, " +
                    E_NAME + " TEXT, " +
                    POLYGONS + " TEXT);";

    public GeoSql(Context context) {
        db = GeoSqlHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

    public void rebuildTable() {
        db.execSQL("DROP TABLE IF EXISTS " + GeoSql.TABLE_NAME);
        db.execSQL(GeoSql.CREATE_TABLE);
    }

    public boolean insert(GeoData geoData) {
        ContentValues cv = new ContentValues();

        cv.put(CODE_103, geoData.CODE_103);
        cv.put(NAME_103, geoData.NAME_103);
        cv.put(D_NAME_103, geoData.D_NAME_103);
        cv.put(E_NAME_103, geoData.E_NAME_103);
        cv.put(CODE, geoData.CODE);
        cv.put(NAME, geoData.NAME);
        cv.put(D_NAME, geoData.D_NAME);
        cv.put(E_NAME, geoData.E_NAME);
        Log.d("json", geoData.CODE_103 + " start");

        cv.put(POLYGONS, Core.gson.toJson(geoData.polygons));
        Log.d("json", geoData.CODE_103 + " end");

        long code = db.insert(TABLE_NAME, null, cv);
        return code != -1;
    }

    public GeoData get(String code) {
        String where = CODE_103 + " = '" + code + "'";
        try (Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, "1")) {
            if (result.moveToFirst()) {
                return getRecord(result);
            }
            return null;
        }
    }

    private GeoData getRecord(Cursor cursor) {
        GeoData geoData = new GeoData();

        geoData.CODE_103 = cursor.getString(0);
        geoData.NAME_103 = cursor.getString(1);
        geoData.D_NAME_103 = cursor.getString(2);
        geoData.E_NAME_103 = cursor.getString(3);

        geoData.CODE = cursor.getString(4);
        geoData.NAME = cursor.getString(5);
        geoData.D_NAME = cursor.getString(6);
        geoData.E_NAME = cursor.getString(7);

        geoData.polygons = Core.gson.fromJson(cursor.getString(8), Polygon[].class);

        return geoData;
    }

}
