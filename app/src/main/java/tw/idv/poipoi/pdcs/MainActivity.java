package tw.idv.poipoi.pdcs;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.dust.capApi.CAP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;

import tw.idv.poipoi.pdcs.database.GeoSqlHelper;
import tw.idv.poipoi.pdcs.database.GeocodeSqlHelper;
import tw.idv.poipoi.pdcs.fragment.CapListHandler;
import tw.idv.poipoi.pdcs.fragment.FriendListHandler;
import tw.idv.poipoi.pdcs.geo.GeoData;
import tw.idv.poipoi.pdcs.net.Callback;
import tw.idv.poipoi.pdcs.user.Response;
import tw.idv.poipoi.pdcs.user.ServerAction;
import tw.idv.poipoi.pdcs.user.User;
import tw.idv.poipoi.pdcs.user.UserCallbacks;
import tw.idv.poipoi.pdcs.user.friend.Friend;

import static tw.idv.poipoi.pdcs.Core.CARE_SERVICE;
import static tw.idv.poipoi.pdcs.Core.CORE;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private static final int REQUEST_ACCESS_LOCATION = 978;

    private DownloadManager downloadManager;
    private static long geodbDownloadId, gcodbDownloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIt = new Intent(this, CareService.class);
        startService(serviceIt);

        /*
        Intent aa = new Intent(this, AlertActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Info", CORE.getCapByIndex(0).info.get(0));
        aa.putExtras(bundle);
        startActivity(aa);
        */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}
                    , REQUEST_ACCESS_LOCATION);
        } else {
            Core.PERMISSION_ACCESS_LOCATION = true;
        }

        initDatabase();
    }

    long totalTime = 0;

    private void updateGeodata() {
        CORE.rebuildDatabase();
        Core.runURLConnect("http://www.poipoi.idv.tw/android_login/files/city_103.kml.json", Charset.forName("big5"), new Callback() {
            @Override
            public void runCallback(Object... params) {
                String s = params[0].toString();
                Log.d("URL", "city_103.kml.json Start");
                GeoData[] gd = Core.gson.fromJson(s, GeoData[].class);
                Log.d("URL", "city_103.kml.json Insert");
                Date before = new Date();
                CORE.setGeodata(gd);
                Date now = new Date();
                totalTime += now.getTime() - before.getTime();
                Log.d("URL", "Insert Time: " + (now.getTime() - before.getTime()));
                Log.d("URL", "city_103.kml.json Done");
            }
        });
        Core.runURLConnect("http://www.poipoi.idv.tw/android_login/files/town_103.kml.json", Charset.forName("big5"), new Callback() {
            @Override
            public void runCallback(Object... params) {
                String s = params[0].toString();
                Log.d("URL", "town_103.kml.json Start");
                GeoData[] gd = Core.gson.fromJson(s, GeoData[].class);
                Log.d("URL", "town_103.kml.json Insert");
                Date before = new Date();
                CORE.setGeodata(gd);
                Date now = new Date();
                totalTime += now.getTime() - before.getTime();
                Log.d("URL", "Insert Time: " + (now.getTime() - before.getTime()));
                Log.d("URL", "town_103.kml.json Done");
            }
        });
        Core.runURLConnect("http://www.poipoi.idv.tw/android_login/files/village_103.kml.json", Charset.forName("big5"), new Callback() {
            @Override
            public void runCallback(Object... params) {
                String s = params[0].toString();
                Log.d("URL", "village_103.kml.json Start");
                GeoData[] gd = Core.gson.fromJson(s, GeoData[].class);
                Log.d("URL", "village_103.kml.json Insert");
                Date before = new Date();
                CORE.setGeodata(gd);
                Date now = new Date();
                totalTime += now.getTime() - before.getTime();
                Log.d("URL", "Insert Time: " + (now.getTime() - before.getTime()));
                Log.d("URL", "village_103.kml.json Done");
            }
        });
        System.out.println("AllDone! TotalTime: " + totalTime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Core.PERMISSION_ACCESS_LOCATION = true;
                    if (!CORE.getCareService().hasLocationService()) {
                        CORE.getCareService().requestLocationService();
                    }
                } else {
                    Core.PERMISSION_ACCESS_LOCATION = false;
                }
                break;
        }
    }

    private void initDatabase() {
        File geodb = getDatabasePath(GeoSqlHelper.DATABASE_NAME);
        if (!geodb.exists()) {
            downloadGeodataDatabase();
        }

        File gcodb = getDatabasePath(GeocodeSqlHelper.DATABASE_NAME);
        if (!gcodb.exists()) {
            downloadGeocodeDatabase();
        }
    }

    private void downloadGeodataDatabase() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://www.poipoi.idv.tw/android_login/files/GeoData.db"));
        request.setDestinationInExternalFilesDir(this, "database", "/" + GeoSqlHelper.DATABASE_NAME)
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
                                copyFile(uri.getPath(), MainActivity.this.getDatabasePath(GeoSqlHelper.DATABASE_NAME).getAbsolutePath());
                                CORE.getCareService().getConfig().setLastestGeoDatabaseTime(new Date());
                                CORE.saveConfig();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                unregisterReceiver(this);
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        geodbDownloadId = downloadManager.enqueue(request);
    }

    private void downloadGeocodeDatabase() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://www.poipoi.idv.tw/android_login/files/GeoCode.db"));
        request.setDestinationInExternalFilesDir(this, "database", "/" + GeocodeSqlHelper.DATABASE_NAME)
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
                                copyFile(uri.getPath(), MainActivity.this.getDatabasePath(GeocodeSqlHelper.DATABASE_NAME).getAbsolutePath());
                                CORE.getCareService().getConfig().setLastestGcoDatabaseTime(new Date());
                                CORE.saveConfig();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                unregisterReceiver(this);
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        gcodbDownloadId = downloadManager.enqueue(request);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        CARE_SERVICE.updateCap();
        super.onResume();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements UserCallbacks {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private CapListHandler capListHandler;
        private FriendListHandler mFriendListHandler;
        private CapListener mCapListener;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            switch (sectionNumber) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_user, container, false);
                    Button mFriendSearchButton = (Button) rootView.findViewById(R.id.button_searchFriend);
                    mFriendSearchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showSearchFriendDialog();
                        }
                    });
                    mFriendListHandler = new FriendListHandler(this.getActivity());
                    mFriendListHandler.setParentList((ListView) rootView.findViewById(R.id.listView_friends));
                    User.getInstance().addListener(this);
                    break;

                case 2:
                    rootView = inflater.inflate(R.layout.fragment_caplist, container, false);
                    capListHandler = new CapListHandler();
                    capListHandler.setParentList((ListView) rootView.findViewById(R.id.listView_capList));
                    mCapListener = new CapListener() {
                        @Override
                        public void onCapChanged(ArrayList<CAP> caps) {
                            capListHandler.notifyDataSetChanged();
                        }
                    };
                    break;
            }

            return rootView;
        }

        private void showSearchFriendDialog() {
            final EditText editText = new EditText(getContext());
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("尋找朋友")
                    .setMessage("輸入朋友的 Email")
                    .setView(editText)
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("搜尋", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            searchFriend(editText.getText().toString());
                        }
                    });
            builder.show();
        }

        private void searchFriend(String email) {
            if (email != null && email.contains("@")) {
                User.getInstance().serverService("http://www.poipoi.idv.tw/android_login/Friend.php?mode=search",
                        new String[]{"friendID=" + email});
            } else {
                Toast.makeText(getContext(), "Email欄位不能為空!", Toast.LENGTH_LONG).show();
            }
        }

        private void showInviteFriendDialog(final String email) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("邀請加入朋友")
                    .setMessage("確定邀請 " + email + " 成為朋友?")
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            User.getInstance().serverService("http://www.poipoi.idv.tw/android_login/Friend.php?mode=invite",
                                    new String[]{"friendID=" + email});
                        }
                    });
            builder.show();
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
            switch (response.getAction()) {
                case ServerAction.FRIEND_ID_FOUND:
                    if ((boolean) response.getData(0)) {
                        showInviteFriendDialog(response.getData(1).toString());
                    } else {
                        String err;
                        switch (response.getData(1).toString()){
                            case "self":
                                err = "你不能邀請自己成為好友";
                                break;
                            case "no_found":
                                err = "找不到此帳號";
                                break;
                            case "already_friend":
                                err = "你和這個人已經是朋友了";
                                break;
                            default:
                                err = "錯誤";
                                break;
                        }
                        Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                    }
                    break;
                case ServerAction.INVITE_FRIEND:
                    if ((boolean) response.getData(0)) {
                        String friendId = response.getData(1).toString();
                        Friend friend = new Friend(friendId, User.getInstance().getUserID());
                        CORE.getFriendSql().insert(friend);
                    } else {
                        Toast.makeText(getContext(), "邀請好友失敗，請稍後重試", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ServerAction.AGREE_FRIEND_INVITE:
                    if ((boolean) response.getData(0)) {
                        String inviterId = response.getData(1).toString();
                        Friend friend = new Friend(inviterId, inviterId);
                        friend.setAgree(true);
                        CORE.getFriendSql().update(friend);
                    } else {
                        Toast.makeText(getContext(), "同意邀請失敗，請稍後重試", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

        @Override
        public void onCheckedLogin(boolean login) {

        }

        @Override
        public void onResume() {
            super.onResume();
            if (mCapListener != null){
                CORE.addCapListener(mCapListener);
                capListHandler.notifyDataSetChanged();
            }
            if (mFriendListHandler != null){
                CORE.getFriendSql().addOnDataChangedListener(mFriendListHandler);
                mFriendListHandler.notifyDataSetChanged();
            }
        }

        @Override
        public void onPause() {
            if (mCapListener != null){
                CORE.removeCapListener(mCapListener);
            }
            if (mFriendListHandler != null){
                CORE.getFriendSql().removeOnDataChangedListener(mFriendListHandler);
            }
            super.onPause();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "使用者";
                case 1:
                    return "警報";
            }
            return null;
        }
    }
}
