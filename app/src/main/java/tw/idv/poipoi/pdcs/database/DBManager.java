package tw.idv.poipoi.pdcs.database;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import tw.idv.poipoi.pdcs.MainActivity;
import tw.idv.poipoi.pdcs.Setting;

import static tw.idv.poipoi.pdcs.Core.CORE;

/**
 * Created by DuST on 2017/12/4.
 */

public class DBManager {

    private Context mContext;
    private DownloadManager downloadManager;
    private static long geodbDownloadId, gcodbDownloadId;

    public DBManager(Context mContext) {
        this.mContext = mContext;
    }

    public void checkDatabase(){
        File geodb = mContext.getDatabasePath(GeoSqlHelper.DATABASE_NAME);
        if (!geodb.exists()) {
            downloadGeodataDatabase();
        }

        File gcodb = mContext.getDatabasePath(GeocodeSqlHelper.DATABASE_NAME);
        if (!gcodb.exists()) {
            downloadGeocodeDatabase();
        }
    }

    private void downloadGeodataDatabase() {
        delOldFile(mContext.getExternalFilesDir("database") + "/" + GeoSqlHelper.DATABASE_NAME);
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Setting.SERVER_DOMAIN + "android_login/files/GeoData.db"));
        request.setDestinationInExternalFilesDir(mContext, "database", "/" + GeoSqlHelper.DATABASE_NAME)
                .setTitle("正在下載地區資料庫")
                .setDescription("請勿取消此下載！");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(geodbDownloadId);
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            Uri uri = Uri.parse(uriString);
                            try {
                                copyFile(uri.getPath(), mContext.getDatabasePath(GeoSqlHelper.DATABASE_NAME).getAbsolutePath());
                                CORE.getCareService().getConfig().setLastestGeoDatabaseTime(new Date());
                                CORE.saveConfig();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                mContext.unregisterReceiver(this);
            }
        };

        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        geodbDownloadId = downloadManager.enqueue(request);
    }

    private void downloadGeocodeDatabase() {
        delOldFile(mContext.getExternalFilesDir("database") + "/" + GeocodeSqlHelper.DATABASE_NAME);
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Setting.SERVER_DOMAIN + "android_login/files/GeoCode.db"));
        request.setDestinationInExternalFilesDir(mContext, "database", "/" + GeocodeSqlHelper.DATABASE_NAME)
                .setTitle("正在下載地區代碼資料庫")
                .setDescription("請勿取消此下載！");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(gcodbDownloadId);
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            Uri uri = Uri.parse(uriString);
                            try {
                                copyFile(uri.getPath(), mContext.getDatabasePath(GeocodeSqlHelper.DATABASE_NAME).getAbsolutePath());
                                CORE.getCareService().getConfig().setLastestGcoDatabaseTime(new Date());
                                CORE.saveConfig();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                mContext.unregisterReceiver(this);
            }
        };

        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        gcodbDownloadId = downloadManager.enqueue(request);
    }

    private boolean delOldFile(String path){
        File file = new File(path);
        return !file.exists() || file.delete();
    }

    private void copyFile(String src, String dest) throws IOException {
        Log.d("IO", "Copy File: " + src + " to " + dest);
        new File(new File(dest).getParent()).mkdirs();
        FileChannel fcs = new FileInputStream(src).getChannel();
        FileChannel fcd = new FileOutputStream(dest).getChannel();
        try {
            fcs.transferTo(0, fcs.size(), fcd);
        } finally {
            fcs.close();
            fcd.close();
        }
    }

}
