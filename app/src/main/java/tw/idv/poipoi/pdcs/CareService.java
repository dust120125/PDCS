package tw.idv.poipoi.pdcs;

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
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.dust.capApi.CAP;
import org.dust.capApi.Severity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import tw.idv.poipoi.pdcs.database.GeocodeSql;
import tw.idv.poipoi.pdcs.geo.City;
import tw.idv.poipoi.pdcs.geo.Town;
import tw.idv.poipoi.pdcs.geo.Village;
import tw.idv.poipoi.pdcs.location.LocationRequester;
import tw.idv.poipoi.pdcs.properties.Config;
import tw.idv.poipoi.pdcs.user.Response;
import tw.idv.poipoi.pdcs.user.ServerAction;
import tw.idv.poipoi.pdcs.user.User;
import tw.idv.poipoi.pdcs.user.UserCallbacks;
import tw.idv.poipoi.pdcs.user.friend.Friend;
import static tw.idv.poipoi.pdcs.Core.CORE;

public class CareService extends Service implements UserCallbacks{

    private static CareService mCareService;

    public static boolean RUNNING_IN_EMULATOR = isOnEmulator();
    public static boolean DOWNLOAD_ALL_CAP = false;
    public static boolean DELETE_EXPIRED = true;

    private boolean started;
    private MyBinder mBinder;

    public static final String CAP_FILE = "caps.c";

    private static final String SERVICE = "Service";
    private static final int UPDATE_STATUS_ID = 1;
    private static final int UPDATE_INTERVAL = (int)(60000 * 5);

    public static final String UPDATE_STATUS = "updateStatus";
    public static final Severity.SeverityCode NOTIFY_SEVERITY = Severity.SeverityCode.Moderate; //嚴重度大於本數值之 CAP 顯示通知訊息
    public static final Severity.SeverityCode ALERT_SEVERITY = Severity.SeverityCode.Extreme; //嚴重度大於本數值之 CAP 顯示警報 Activity

    private User mUser;

    public Config getConfig() {
        return config;
    }
    private static Config config;

    private GeocodeSql GEO_CODE_SQL;
    private AlarmReceiver alarmReceiver;
    private LocationRequester mLocationRequester;

    public CapManager getCapManager() {
        return capManager;
    }

    private CapManager capManager;

    public CareService() {
    }

    public static CareService getInstance(){
        return mCareService;
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        mCareService = this;
        Log.d(SERVICE, "onCreate");
        Log.i("Thread", "Service: " + Thread.currentThread().getId());
        Log.i("Firebase", "Token: " + FirebaseInstanceId.getInstance().getToken());

        config = Core.getConfig();
        mUser = User.getInstance();
        mUser.addListener(this);
        capManager = new CapManager(getApplicationContext(), config);

        mBinder = new MyBinder();

        if (!mUser.isLogin()) {
            if (!mUser.isCheckedLogin()) {
                mUser.checkLogin();
            }
        } else {
            startOn();
        }
    }

    private void startOn(){
        Log.i(SERVICE, "StartOn");
        alarmReceiver = new AlarmReceiver();
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
                if (!hasGcoDatabase()) return;
                try {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.TAIWAN);
                    /*
                    if (RUNNING_IN_EMULATOR){
                        location.setLatitude(22.5005421);
                        location.setLongitude(120.3835537);
                    }
                    */
                    address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                    if (address == null) return;
                    Log.i("Address", "getAddressLine: " + address.getAddressLine(0));
                    Log.i("Address", "getAdminArea: " + address.getAdminArea());
                    Log.i("Address", "getLocality: " + address.getLocality());
                    Log.i("Address", "getSubAdminArea: " + address.getSubAdminArea());
                    Log.i("Address", "getSubLocality: " + address.getSubLocality());
                    if (address.getAdminArea() != null && address.getLocality() != null) {
                        Log.d("Location", address.getAddressLine(0));
                        String townCode = getTown(address.getAdminArea() + address.getLocality()).code_103;
                        if (townCode != null) {
                            capManager.setCurrentGeocode(townCode);
                            return;
                        }
                    }
                    capManager.setCurrentGeocode(capManager.getCurrentGeocode());
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

        started = true;
    }

    public void updateCap(){
        capManager.downloadNewCaps(config.getLastestCapUpdateTime());
    }

    public boolean requestLocationService(){
        return mLocationRequester.requestLocationService();
    }

    public boolean hasLocationService(){
        return mLocationRequester.hasLocationService();
    }

    public boolean hasGeoDatabase() {
        return !(config.getLastestGeoDatabaseTime() == null);
    }

    public boolean hasGcoDatabase() {
        return !(config.getLastestGcoDatabaseTime() == null);
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
        //super.onTaskRemoved(rootIntent);
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
        if (started) {
            saveCapList(capManager.getCapList());
            unregisterReceiver(alarmReceiver);
        }
        saveConfig();
        Log.i(SERVICE, "onClosing");
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

    public void saveConfig() {
        Core.saveConfig();
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
        if (!started) {
            startOn();
        }
    }

    @Override
    public void onLoginFail(String error) {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public void onReceive(Response response) {
        switch (response.getAction()){
            case ServerAction.RECEIVE_FCM_MESSAGE:
                handleFcmMessage((Map<String, Object>) response.getData(0));
                break;
        }
    }

    @Override
    public void onCheckedLogin(boolean login) {
        Log.i(SERVICE, "CheckedLogin: " + login);
        if (!login){
            //stopSelf();
        }
    }

    private void handleFcmMessage(Map<String, Object> data){
        try {
            int action = Integer.parseInt(data.get("action").toString());
            switch (action){
                case ServerAction.INVITE_FRIEND:
                    if (data.get("target").equals(User.getInstance().getUserID())){
                        String inviter = data.get("inviter").toString();
                        Friend friend = new Friend(inviter, inviter);
                        CORE.getFriendSql().insert(friend);
                    }
                    break;
                case ServerAction.AGREE_FRIEND_INVITE:
                    if (data.get("inviter").equals(User.getInstance().getUserID())){
                        String target = data.get("target").toString();
                        String inviter = data.get("inviter").toString();
                        Friend friend = new Friend(target, inviter);
                        friend.setAgree(true);
                        CORE.getFriendSql().update(friend);
                    }
                    break;
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
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