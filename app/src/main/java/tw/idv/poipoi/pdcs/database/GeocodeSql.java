package tw.idv.poipoi.pdcs.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import tw.idv.poipoi.pdcs.geo.City;
import tw.idv.poipoi.pdcs.geo.Town;
import tw.idv.poipoi.pdcs.geo.Village;

/**
 * Created by DuST on 2017/5/22.
 */

public class GeocodeSql {

    private SQLiteDatabase db;
    private static final String TABLE_CITY = "geo_city";
    private static final String TABLE_TOWN = "geo_town";
    private static final String TABLE_VILLAGE = "geo_village";

    public static final int CITY_CODE = 11;
    public static final int CITY_NAME = 12;

    public static final int TOWN_CODE = 21;
    public static final int TOWN_FULLNAME = 22;

    public GeocodeSql(Context context) {
        this.db = GeocodeSqlHelper.getDatabase(context);
    }

    //region Get City
    public City getCity(String value, int mode) {
        String where;
        if (mode == CITY_CODE){
            where = "code_103 = '" + value + "'";
        } else {
            where = "name_103 = '" + value + "'";
        }

        try (Cursor result = db.query(TABLE_CITY, null, where, null, null, null, null, "1")) {
            if (result.moveToFirst()) {
                return getCityRecord(result);
            }
            return null;
        }
    }

    private City getCityRecord(Cursor cursor) {
        City city = new City();

        city.eName = cursor.getString(0);
        city.fName = cursor.getString(1);
        city.name = cursor.getString(2);
        city.code = cursor.getString(3);

        city.eName_103 = cursor.getString(4);
        city.fName_103 = cursor.getString(5);
        city.name_103 = cursor.getString(6);
        city.code_103 = cursor.getString(7);

        return city;
    }
    //endregion

    //region Get Town
    public Town getTown(String city, String town) {
        String where = "city_103 = '" + city + "' AND name_103 = '" + town + "'";

        try (Cursor result = db.query(TABLE_TOWN, null, where, null, null, null, null, "1")) {
            if (result.moveToFirst()) {
                return getTownRecord(result);
            }
            return null;
        }
    }

    public Town getTown(String value, int mode) {
        String where;
        if (mode == TOWN_CODE){
            where = "code_103 = '" + value + "'";
        } else {
            where = "fName_103 = '" + value + "'";
        }

        try (Cursor result = db.query(TABLE_TOWN, null, where, null, null, null, null, "1")) {
            if (result.moveToFirst()) {
                return getTownRecord(result);
            }
            return null;
        }
    }

    private Town getTownRecord(Cursor cursor) {
        Town town = new Town();

        town.eName = cursor.getString(0);
        town.fName = cursor.getString(1);
        town.code = cursor.getString(2);
        town.name = cursor.getString(3);

        town.eName_103 = cursor.getString(4);
        town.fName_103 = cursor.getString(5);
        town.city_103 = cursor.getString(6);
        town.name_103 = cursor.getString(7);
        town.code_103 = cursor.getString(8);

        return town;
    }
    //endregion

    //region Get Village
    public Village getVillage(String city, String town, String village) {
        String where = "city_103 = '" + city + "' AND town_103 = '" + town + "' AND name_103 = '" + village + "'";

        try (Cursor result = db.query(TABLE_VILLAGE, null, where, null, null, null, null, "1")) {
            if (result.moveToFirst()) {
                return getVillageRecord(result);
            }
            return null;
        }
    }

    public Village getVillage(String code) {
        String where = "code_103 = '" + code + "'";

        try (Cursor result = db.query(TABLE_VILLAGE, null, where, null, null, null, null, "1")) {
            if (result.moveToFirst()) {
                return getVillageRecord(result);
            }
            return null;
        }
    }

    private Village getVillageRecord(Cursor cursor) {
        Village village = new Village();

        village.name = cursor.getString(0);
        village.town = cursor.getString(1);
        village.city = cursor.getString(2);
        village.code = cursor.getString(3);

        village.name_103 = cursor.getString(4);
        village.town_103 = cursor.getString(5);
        village.city_103 = cursor.getString(6);
        village.code_103 = cursor.getString(7);

        return village;
    }
    //endregion
}
