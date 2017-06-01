package tw.idv.poipoi.pdcs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.dust.capApi.AlertStatus;
import org.dust.capApi.CAP;
import org.dust.capApi.Info;
import org.dust.capApi.Severity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import tw.idv.poipoi.pdcs.alert.AlertFactory;
import tw.idv.poipoi.pdcs.net.Callback;
import tw.idv.poipoi.pdcs.net.URLConnectRunner;
import tw.idv.poipoi.pdcs.properties.Config;

/**
 * Created by DuST on 2017/5/23.
 */

public class CapManager implements Serializable, CapListener {

    private Config config;
    private Context appContext;

    private HashMap<String, CAP> CAP_MAP;
    private ArrayList<CAP> CAP_LIST;
    private ArrayList<String> updateList;

    private static final int UPDATE_LIST_LIMIT = 64;
    private static final long EXPRIED_CAP_DEADLINE = TimeUnit.DAYS.toMillis(2); //2 days

    private List<CapListener> capListeners;
    private int notifiesID = 0;

    private String currentGeocode;
    private boolean waitForGeocode = false;

    public CapManager(Context context, Config config) {
        this.config = config;
        this.appContext = context;
        CAP_MAP = new HashMap<>();
        CAP_LIST = new ArrayList<>();
        updateList = new ArrayList<>();
        capListeners = new LinkedList<>();
    }

    public void downloadAllCaps(boolean expired) {
        Core.getCap(expired, new Callback() {
            @Override
            public void runCallback(Object... params) {
                CAP[] cap = Core.gson.fromJson((String) params[0], CAP[].class);
                insertAll(Arrays.asList(cap));
                config.setLastestCapUpdateTime(new Date());
            }
        });
    }

    public void downloadNewCaps(Date lastUpdateDate){
        if (lastUpdateDate == null){
            downloadAllCaps(false);
        } else {
            Log.i("CAP", "UpdateCap, LastTime: " + lastUpdateDate.toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd%20HH:mm:ss", Locale.TAIWAN);
            String dateStr = sdf.format(lastUpdateDate);
            //String url = "http://www.poipoi.idv.tw/android_login/CheckUpdate.php?time='" + dateStr + "'";
            String url = "http://www.poipoi.idv.tw/android_login/CheckUpdate2.php";
            new URLConnectRunner(url, null, Charset.defaultCharset(), new Callback() {
                @Override
                public void runCallback(Object... params) {
                    String result = (String) params[0];
                    if (result.equals("null")) return;
                    String[] capIds = result.split(",");
                    List<String> idList = new LinkedList<>();
                    for (String id : capIds) {
                        if (!(CAP_MAP.containsKey(id) || updateList.contains(id))){
                            idList.add(id);
                        }
                    }
                    Log.i("CAP", "UpdateCap, " + (capIds.length - idList.size()) + " repeat, " + idList.size() + " new");
                    if (idList.size() > 0) {
                        downloadCap(idList.toArray(capIds));
                    }
                }
            });
        }
    }

    public void updateData(){
        removeExpriedCaps();
        downloadNewCaps(config.getLastestCapUpdateTime());
    }

    public void removeExpriedCaps(){
        if (!CareService.DELETE_EXPIRED) return;
        List<CAP> list = new LinkedList<>();
        for (CAP c : CAP_LIST) {
            if (c.getExpiredTimePast() >= EXPRIED_CAP_DEADLINE){
                list.add(c);
                CAP_MAP.remove(c.identifier);
            }
        }
        if (list.size() > 0) {
            CAP_LIST.removeAll(list);
            Log.i("CAP", "Removed " + list.size() + " expried CAPs");
            notifyDataSetChanged(CAP_LIST);
        }
    }

    public void insert(CAP cap) {
        if (!CAP_MAP.containsKey(cap.identifier)) {
            CAP_MAP.put(cap.identifier, cap);
            CAP_LIST.add(cap);
        }
        onCapChanged(CAP_LIST);
    }

    public void insertAll(Collection<CAP> caps) {
        Log.i("CAP", "Download CAPs: " + caps.size());
        int count = 0;
        for (CAP cap : caps) {
            capUpdateCheck(cap);
            if (!CAP_MAP.containsKey(cap.identifier)) {
                CAP_MAP.put(cap.identifier, cap);
                CAP_LIST.add(cap);
                count++;
            }
        }
        if (count > 0) {
            onCapChanged(CAP_LIST);
        }
        Log.i("CAP", "Update CAPs: " + count);
    }

    public void capUpdateCheck(CAP cap){
        if (cap.msgType.equals("Update")) {
            String targetId = cap.references.split(",")[1];
            CAP replaced = CAP_MAP.remove(targetId);
            String replacedId = replaced == null ? "null" : replaced.identifier;
            Log.i("CAP", "Replace: " + cap.identifier + " -> " + replacedId);
            if (replaced != null) {
                CAP_LIST.remove(replaced);
                updateList.add(targetId);
                checkUpdateListSize();
                capUpdateCheck(replaced);
            }
        }
    }

    public void checkUpdateListSize(){
        if (updateList.size() > UPDATE_LIST_LIMIT){
            int removeCount = UPDATE_LIST_LIMIT / 2;
            for(int i = 0; i < removeCount; i++){
                updateList.remove(0);
            }
        }
    }

    public void updateActualEffect(String geocode) {
        Log.i("CAP", "updateActualEffect: " + geocode);
        List<CAP> notifies = new LinkedList<>();
        for (CAP cap : CAP_LIST) {
            Severity.SeverityCode severity = cap.updateActualEffect(geocode);

            if (cap.getStatus() != AlertStatus.EXPIRED) {
                for (Info info : cap.info) {
                    if (needAlert(info)) {
                        showAlertActivity(info);
                    }
                }
                if (!cap.isNotified() && Severity.compare(severity, CareService.NOTIFY_SEVERITY) >= 0) {
                    notifies.add(cap);
                }
            }
        }
        showNotification(notifies);
        Collections.sort(CAP_LIST);
        notifyDataSetChanged(CAP_LIST);
        waitForGeocode = false;
    }

    private void showNotification(Collection<CAP> caps){
        NotificationManager nm = (NotificationManager) appContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        for(CAP c : caps){
            for(int i = 0; i < c.info.size(); i++) {
                Info ifo = c.info.get(i);
                if (Severity.compare(ifo.getActualEffect(), CareService.NOTIFY_SEVERITY) >= 0) {
                    Intent intent = new Intent(appContext, MapsActivity.class);
                    intent.putExtra("capId", c.identifier);
                    intent.putExtra("infoIndex", i);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
                    stackBuilder.addParentStack(MapsActivity.class);
                    stackBuilder.addNextIntent(intent);

                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(notifiesID, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new Notification.Builder(appContext)
                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                            .setContentTitle(ifo.event)
                            .setContentText(ifo.description)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build();
                    nm.notify(notifiesID++, notification);
                    c.setNotified(true);
                }
            }
        }
    }

    private boolean needAlert(CAP cap){
        return AlertFactory.needAlert(cap);
    }

    private boolean needAlert(Info info){
        return AlertFactory.needAlert(info);
    }

    public void showAlertActivity(Info info){
        if (Core.onAlertActivity) return;
        Intent intent = new Intent(appContext, AlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Info", info);
        intent.putExtras(bundle);
        appContext.startActivity(intent);
    }

    @Override
    public void onCapChanged(ArrayList<CAP> caps) {
        if (currentGeocode != null) {
            updateActualEffect(currentGeocode);
        }
        notifyDataSetChanged(caps);
        waitForGeocode = true;
    }

    private void notifyDataSetChanged(ArrayList<CAP> caps){
        for (CapListener listener : capListeners) {
            listener.onCapChanged(caps);
        }
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

    public void setCurrentGeocode(String currentGeocode) {
        if (waitForGeocode) {
            updateActualEffect(currentGeocode);
        } else if (this.currentGeocode == null || !this.currentGeocode.equals(currentGeocode)){
            updateActualEffect(currentGeocode);
        }
        this.currentGeocode = currentGeocode;
    }

    public String getCurrentGeocode() {
        return currentGeocode;
    }

    public void downloadCap(String... caps) {
        AsyncTask<String, Void, String> urlPostTask = new AsyncTask<String, Void, String>() {

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected void onPostExecute(String s) {
                config.setLastestCapUpdateTime(new Date());
                Gson gson = new Gson();
                CAP[] caps = gson.fromJson(s, CAP[].class);
                if (caps != null && caps.length > 0) {
                    insertAll(Arrays.asList(caps));
                }
            }

            @Override
            protected String doInBackground(String... caps) {
                StringBuilder stringBuilder = new StringBuilder("batch=");
                for (String c : caps) {
                    stringBuilder.append(c);
                    stringBuilder.append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                String postData = stringBuilder.toString();

                String result = null;
                try {
                    URL url = new URL("http://www.poipoi.idv.tw/android_login/ServerApib.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");

                    con.setDoOutput(true);
                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                    dos.writeBytes(postData);
                    dos.flush();
                    dos.close();


                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
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
        urlPostTask.execute(caps);
    }
}
