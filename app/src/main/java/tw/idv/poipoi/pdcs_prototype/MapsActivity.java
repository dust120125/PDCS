package tw.idv.poipoi.pdcs_prototype;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Map;

import org.dust.capApi.Area;
import org.dust.capApi.CAP;
import org.dust.capApi.Info;
import org.dust.capApi.Resource;
import org.dust.capApi.Severity;
import org.dust.capApi.Value;
import tw.idv.poipoi.pdcs_prototype.geo.GeoData;
import tw.idv.poipoi.pdcs_prototype.geo.Polygon;
import tw.idv.poipoi.pdcs_prototype.maps.MapDrawer;

import static tw.idv.poipoi.pdcs_prototype.Core.CORE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TableLayout table_CapInfo;
    private LayoutInflater mInflater;
    private MyHandler mHandler;

    public static final int ADD_POLYGON = 11;
    public static final int ADD_CIRCLE = 12;
    public static final int ADD_MARKER = 13;

    private CAP cap;
    private Info info;

    private static TableRow.LayoutParams LAYOUT_MATCH_PARENT
            = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

    private static class MyHandler extends Handler{

        private GoogleMap mMap;

        public void setGoogleMap(GoogleMap mMap) {
            this.mMap = mMap;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ADD_POLYGON:
                    mMap.addPolygon((PolygonOptions) msg.obj);
                    break;
                case ADD_CIRCLE:
                    mMap.addCircle((CircleOptions) msg.obj);
                    break;
                case ADD_MARKER:
                    mMap.addMarker((MarkerOptions) msg.obj);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mHandler = new MyHandler();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mInflater = getLayoutInflater();
        table_CapInfo = (TableLayout) findViewById(R.id.table_CapInfo);

        Intent intent = this.getIntent();
        String capId = intent.getStringExtra("capId");
        int infoIndex = intent.getIntExtra("infoIndex", 0);

        cap = CORE.getCapById(capId);
        info = cap.info.get(infoIndex);
        createDataRows();
    }

    private void addRow(View view){
        table_CapInfo.addView(view);
    }

    private void createDataRows(){
        addRow(getRow("類別: ", info.event));
        addRow(getRow("威脅度: ", Severity.getSeverityText(info.severity)));
        addRow(getRow("發生機率: ", Info.getCertaintyText(info.certainty)));
        addRow(getRow("緊急程度: ", Info.getUrgency(info.urgency)));
        addRow(getRow("敘述: ", info.description));
        if (info.instruction != null && !info.instruction.isEmpty()){
            addRow(getRow("建議方案: ", info.instruction));
        }
        addRow(getRow("生效時間: ", info.effective));
        addRow(getRow("過期時間: ", info.expires));
        addRow(getRow("發布單位: ", info.senderName));
        if (info.web != null && !info.web.isEmpty()){
            addRow(getRow("參考網頁: ", info.web));
        }
        for(Resource r : info.resource){
            addRow(getRow(r.resourceDesc + ": ", r.uri));
        }
    }

    private View getRow(String title, String content){
        View view = mInflater.inflate(R.layout.tablerow_capinfo, null);
        ((TextView) view.findViewById(R.id.textView_rowTitle)).setText(title);
        TextView contentText = ((TextView) view.findViewById(R.id.textView_rowCotent));
        contentText.setAutoLinkMask(Linkify.WEB_URLS);
        contentText.setText(content);
        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mHandler.setGoogleMap(googleMap);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(23.4985179, 120.7968259);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(7.75f));

        if (CORE.hasGeoDatabase()) {
            AsyncTask<Void, Void, Void> loadinGeodate = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    MapDrawer md = new MapDrawer(info, mHandler);
                    md.draw();
                    return null;
                }
            };
            loadinGeodate.execute();
        } else {
            Toast.makeText(this, "區域資料庫未下載完成！", Toast.LENGTH_LONG).show();
        }
    }
}
