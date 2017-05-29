package tw.idv.poipoi.pdcs;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.gson.Gson;

import org.dust.util.LruCache;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.dust.capApi.CAP;
import tw.idv.poipoi.pdcs.database.GeoSql;
import tw.idv.poipoi.pdcs.geo.GeoData;
import tw.idv.poipoi.pdcs.fragment.CapListHandler;
import tw.idv.poipoi.pdcs.location.LocationRequester;
import tw.idv.poipoi.pdcs.net.Callback;
import tw.idv.poipoi.pdcs.net.URLConnectRunner;
import tw.idv.poipoi.pdcs.properties.Config;

/**
 * Created by DuST on 2017/3/4.
 */

public class Core extends Application {

    public static Core CORE;
    public static CareService CARE_SERVICE;
    public static Gson gson = new Gson();
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);

    public Config config;

    public static boolean PERMISSION_ACCESS_LOCATION = false;

    private GeoSql GEO_SQL;
    private LruCache<String, GeoData> geoDataLruCache;
    private LocationRequester mLocationRequester;


    private HashMap<String, CAP> CAP_MAP = new HashMap<>();
    private ArrayList<CAP> CAP_LIST = new ArrayList<>();

    private List<CapListener> capListeners = new LinkedList<>();
    private CapListHandler CAP_LIST_HANDLER;

    private static final int GEODATA_CACHE_SIZE = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext(), "AppCrash"));
        CORE = this;
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        geoDataLruCache = new LruCache<>(GEODATA_CACHE_SIZE);
        initService();
    }

    public void saveConfig(){
        CARE_SERVICE.saveConfig();
    }

    private void initService(){
        Intent serviceIt = new Intent(this, CareService.class);
        startService(serviceIt);
        bindService(serviceIt,
                new LocalServiceConnection(),
                BIND_AUTO_CREATE);
    }

    public boolean hasGeoDatabase(){
        return CARE_SERVICE.hasGeoDatabase();
        //return !(config.getLastestGeoDatabaseTime() == null);
    }

    public boolean hasGcoDatabase(){
        return CARE_SERVICE.hasGcoDatabase();
    }

    public CareService getCareService(){
        return CARE_SERVICE;
    }

    public static void getCap(String capId, Callback callback) {
        runURLConnect(
                "http://www.poipoi.idv.tw/android_login/ServerApib.php?capId=" + capId,
                Charset.defaultCharset(),
                callback
        );
    }

    public static void getCap(boolean expired, Callback callback) {
        runURLConnect(
                "http://www.poipoi.idv.tw/android_login/ServerApib.php?expired=" + expired,
                Charset.defaultCharset(),
                callback
        );
    }

    public CAP getCapByIndex(int index) {
        try {
            return CAP_LIST.get(index);
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public CAP getCapById(String capId) {
        return CAP_MAP.get(capId);
    }

    public HashMap<String, CAP> getCapMap() {
        return CAP_MAP;
    }

    public ArrayList<CAP> getCapList() {
        return CAP_LIST;
    }

    public void addCapListener(CapListener listener) {
        capListeners.add(listener);
    }

    public void removeCapListener(CapListener listener) {
        capListeners.remove(listener);
    }

    public void removeLocationListener(LocationListener listener) {
        mLocationRequester.removeLocationListener(listener);
    }

    public void CapChanged() {
        for (CapListener listener : capListeners) {
            if (listener != null)
                listener.onCapChanged(CAP_LIST);
        }
    }

    public void notifyCapDataUpdate() {
        if (CAP_LIST_HANDLER != null)
            CAP_LIST_HANDLER.notifyDataSetChanged();
    }

    public static Context getMainContext() {
        return CORE.getApplicationContext();
    }

    public void rebuildDatabase() {
        if (GEO_SQL == null) {
            GEO_SQL = new GeoSql(getMainContext());
        }
        GEO_SQL.rebuildTable();
    }

    public void setGeodata(GeoData[] data) {
        if (GEO_SQL == null) {
            GEO_SQL = new GeoSql(getMainContext());
        }
        for (GeoData g : data) {
            Log.d("insert: ", g.CODE_103);
            GEO_SQL.insert(g);
        }
    }

    public GeoData getGeodata(String type, String key) {
        GeoData result = geoDataLruCache.get(key);
        if (result == null) {
            if (GEO_SQL == null) {
                GEO_SQL = new GeoSql(getMainContext());
            }
            result = GEO_SQL.get(key);
            geoDataLruCache.put(key, result);
            return result;
        } else {
            return result;
        }
    }

    public static void runURLConnect(String url, Charset charset, Callback callback) {
        new URLConnectRunner(url, null, charset, callback);
    }

    private static class URLConnectRunner2 {

        public URLConnectRunner2(String url, final Charset charset, final Callback callback) {
            AsyncTask<String, Void, String> urlTask = new AsyncTask<String, Void, String>() {

                Callback cb;

                @Override
                protected void onPreExecute() {
                    this.cb = callback;
                }

                @Override
                protected void onPostExecute(String s) {
                    if (cb != null)
                        cb.runCallback(s);
                }

                @Override
                protected String doInBackground(String... params) {
                    String result = null;
                    try {
                        URL url = new URL(params[0]);
                        URLConnection con = url.openConnection();
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(con.getInputStream(), charset));
                        StringWriter sw = new StringWriter();
                        String tmp;
                        while ((tmp = br.readLine()) != null) {
                            sw.write(tmp);
                        }
                        result = sw.toString();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                    //Log.d("URL result", result);
                    return result;
                }
            };
            urlTask.execute(url);
        }
    }

    private class LocalServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CARE_SERVICE = (CareService) ((ServiceBinder) service).getService();
            CARE_SERVICE.test();
            CapManager cm = CARE_SERVICE.getCapManager();
            CAP_MAP = cm.getCapMap();
            CAP_LIST = cm.getCapList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CARE_SERVICE = null;
        }
    }

}
