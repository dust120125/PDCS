package tw.idv.poipoi.pdcs_prototype;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

import org.dust.capApi.CAP;
import org.dust.capApi.Severity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import tw.idv.poipoi.pdcs_prototype.database.GeocodeSql;
import tw.idv.poipoi.pdcs_prototype.geo.City;
import tw.idv.poipoi.pdcs_prototype.geo.Town;
import tw.idv.poipoi.pdcs_prototype.geo.Village;
import tw.idv.poipoi.pdcs_prototype.location.LocationRequester;
import tw.idv.poipoi.pdcs_prototype.net.Callback;
import tw.idv.poipoi.pdcs_prototype.properties.Config;
import tw.idv.poipoi.pdcs_prototype.user.Response;
import tw.idv.poipoi.pdcs_prototype.user.User;
import tw.idv.poipoi.pdcs_prototype.user.UserCallbacks;

public class CareService extends Service implements UserCallbacks{

    public static boolean RUNNING_IN_EMULATOR = isOnEmulator();
    public static boolean DOWNLOAD_ALL_CAP = false;
    public static boolean DELETE_EXPIRED = true;

    public static String android_id;

    private MyBinder mBinder;
    public static final String CONFIG_FILE = "config.ini";
    public static final String CAP_FILE = "caps.c";

    private static final String SERVICE = "Service";
    private static final int UPDATE_STATUS_ID = 1;
    private static final int UPDATE_INTERVAL = 60000 * 5;

    public static final String UPDATE_STATUS = "updateStatus";
    public static final Severity.SeverityCode NOTIFY_SEVERITY = Severity.SeverityCode.Moderate; //嚴重度大於本數值之 CAP 顯示通知訊息
    public static final Severity.SeverityCode ALERT_SEVERITY = Severity.SeverityCode.Extreme; //嚴重度大於本數值之 CAP 顯示警報 Activity

    private User mUser;

    public Config getConfig() {
        return config;
    }
    private Config config;

    private GeocodeSql GEO_CODE_SQL;
    private AlarmReceiver alarmReceiver;
    private LocationRequester mLocationRequester;

    public CapManager getCapManager() {
        return capManager;
    }

    private CapManager capManager;

    public CareService() {
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        Log.d(SERVICE, "onCreate");
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext(), "AppCrash"));
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        loadConfig();
        mUser = User.getInstance();
        mUser.addListener(this);
        mUser.checkLogin();

        mBinder = new MyBinder();
        alarmReceiver = new AlarmReceiver();
        capManager = new CapManager(getApplicationContext(), config);
        //capManager.downloadAllCaps();
        capManager.downloadNewCaps(config.getLastestCapUpdateTime());
        //capManager.downloadNewCaps(Date.valueOf("2017-05-24"));
        capManager.addCapListener(new CapListener() {
            @Override
            public void onCapChanged(ArrayList<CAP> caps) {
                if (Core.CORE != null) {
                    Core.CORE.CapChanged();
                }
            }
        });

        if (DOWNLOAD_ALL_CAP){
            capManager.downloadAllCaps(true);
        }
        loadCapList();
        setAlarm();

        mLocationRequester = new LocationRequester(this.getApplicationContext());
        addLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Address address = null;
                try {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.TAIWAN);
                    if (RUNNING_IN_EMULATOR){
                        location.setLatitude(22.5005421);
                        location.setLongitude(120.3835537);
                    }
                    address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                    Log.d("Location", address.getAddressLine(0));
                    String townCode = getTown(address.getAdminArea() + address.getLocality()).code_103;
                    if (townCode != null) {
                        capManager.setCurrentGeocode(townCode);
                    } else {
                        capManager.setCurrentGeocode(capManager.getCurrentGeocode());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    RuntimeException re = new RuntimeException(
                            address.getAddressLine(0)
                                    + "\ngetAdminArea() = " + address.getAdminArea()
                                    + "\ngetLocality() = " + address.getLocality(), e);
                    re.setStackTrace(e.getStackTrace());
                    throw re;
                }
            }
        });
    }

    public boolean requestLocationService(){
        return mLocationRequester.requestLocationService();
    }

    public boolean hasLocationService(){
        return mLocationRequester.hasLocationService();
    }

    private void setAlarm() {
        registerReceiver(alarmReceiver, new IntentFilter(UPDATE_STATUS));
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent it = new Intent(UPDATE_STATUS);
        PendingIntent pi = PendingIntent.getBroadcast(this, UPDATE_STATUS_ID,
                it, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), UPDATE_INTERVAL, pi);
    }

    private void cancelAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent it = new Intent(UPDATE_STATUS);
        PendingIntent pi = PendingIntent.getBroadcast(this, UPDATE_STATUS_ID,
                it, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        unregisterReceiver(alarmReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(SERVICE, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(SERVICE, "onTaskRemoved");
        onClosing();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(SERVICE, "onDestroy");
        onClosing();
        //cancelAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void onClosing(){
        saveCapList(capManager.getCapList());
        unregisterReceiver(alarmReceiver);
        saveConfig();
    }

    private void saveCapList(Collection<CAP> caps){
        /*
        List<CAP> target = new LinkedList<>();
        for (CAP c : caps) {
            if (c.getStatus() != AlertStatus.EXPIRED){
                target.add(c);
            }
        }
        */

        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(CAP_FILE, MODE_PRIVATE));
            oos.writeObject(caps);
            oos.flush();
            oos.close();
            Log.i("Service", "Closing, Saved: " + caps.size() + " CAPs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCapList(){
        try {
            ObjectInputStream ois = new ObjectInputStream(openFileInput(CAP_FILE));
            Collection<CAP> caps = (Collection<CAP>) ois.readObject();
            capManager.insertAll(caps);
            Log.i("Service", "onCreate, Loaded: " + caps.size() + " CAPs");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        if (PropertiesManager.existsProperties(getApplicationContext(), CONFIG_FILE)) {
            try {
                config = new Config(getApplicationContext(),
                        PropertiesManager.getProperties(getApplicationContext(), CONFIG_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = new Config(getApplicationContext());
        }
    }

    public void saveConfig() {
        try {
            PropertiesManager.saveProperties(getApplicationContext(), config.getProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasGeoDatabase() {
        return !(config.getLastestGeoDatabaseTime() == null);
    }

    public boolean hasGcoDatabase() {
        return !(config.getLastestGcoDatabaseTime() == null);
    }

    public City getCity(String name) {
        City result;
        if (GEO_CODE_SQL == null) {
            GEO_CODE_SQL = new GeocodeSql(getApplicationContext());
        }
        result = GEO_CODE_SQL.getCity(name, GeocodeSql.CITY_NAME);
        return result;
    }

    public Town getTown(String fullName) {
        Town result;
        if (GEO_CODE_SQL == null) {
            GEO_CODE_SQL = new GeocodeSql(getApplicationContext());
        }
        result = GEO_CODE_SQL.getTown(fullName, GeocodeSql.TOWN_FULLNAME);
        return result;
    }

    public Village getVillage(String city, String town, String village) {
        Village result;
        if (GEO_CODE_SQL == null) {
            GEO_CODE_SQL = new GeocodeSql(getApplicationContext());
        }
        result = GEO_CODE_SQL.getVillage(city, town, village);
        return result;
    }

    public void addLocationListener(LocationListener listener) {
        mLocationRequester.addLocationListener(listener);
    }

    public static boolean isOnEmulator(){
        Log.i("Build.FINGERPRINT", Build.FINGERPRINT);
        Log.i("Build.MODEL", Build.MODEL);
        Log.i("Build.MANUFACTURER", Build.MANUFACTURER);
        Log.i("Build.BRAND", Build.BRAND);
        Log.i("Build.DEVICE", Build.DEVICE);
        Log.i("Build.PRODUCT", Build.PRODUCT);

        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public void test() {
        Log.d(SERVICE, "Test");
    }

    @Override
    public void onLogin() {

    }

    @Override
    public void onLoginFail(String error) {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public void onReceive(Response response) {

    }

    private class MyBinder extends ServiceBinder {

        @Override
        public Service getService() {
            return CareService.this;
        }
    }

    private class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Service", "Received broadcast: " + intent.getAction());
            capManager.updateData();
        }
    }
}