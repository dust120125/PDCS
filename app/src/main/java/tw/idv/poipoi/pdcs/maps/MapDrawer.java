package tw.idv.poipoi.pdcs.maps;

import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.dust.capApi.Area;
import org.dust.capApi.Info;
import org.dust.capApi.Value;

import java.util.Map;

import tw.idv.poipoi.pdcs.HandlerCode;
import tw.idv.poipoi.pdcs.MapsActivity;
import tw.idv.poipoi.pdcs.SeverityColor;
import tw.idv.poipoi.pdcs.geo.GeoData;
import tw.idv.poipoi.pdcs.geo.Polygon;

import static tw.idv.poipoi.pdcs.Core.CORE;

/**
 * Created by DuST on 2017/5/28.
 */

public class MapDrawer {

    private Info info;
    private Handler mHandler;

    public MapDrawer(Info info, Handler mHandler) {
        this.info = info;
        this.mHandler = mHandler;
    }

    public void draw(){
        Map<String, Area> areas = info.getCommonSeverityArea();
        for (Area a : areas.values()) {
            for (Value v : a.geocode) {
                GeoData gd = CORE.getGeodata("103", v.value);
                for (Polygon p : gd.polygons) {
                    PolygonOptions po = new PolygonOptions()
                            .add(p.latlngs)
                            .strokeWidth(4.0f)
                            .fillColor(SeverityColor.getSeverityColor(a.severity));
                    addPolygon(po);
                }
            }
            for (String cir : a.circle) {
                String[] cirAtt = cir.split(" ");
                String[] coordinate = cirAtt[0].split(",");
                double radius = Double.parseDouble(cirAtt[1]) * 1000;
                double lat = Double.parseDouble(coordinate[0]);
                double lng = Double.parseDouble(coordinate[1]);
                if (radius > 0){
                    CircleOptions co = new CircleOptions()
                            .center(new LatLng(lat, lng))
                            .radius(radius)
                            .fillColor(SeverityColor.getSeverityColor(a.severity))
                            .strokeWidth(4.0f);
                    addCircle(co);
                }
                MarkerOptions mo = new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(a.areaDesc.trim())
                        .snippet(info.description.trim());
                addMarker(mo);
            }
        }
    }

    private void addPolygon(PolygonOptions polygonOptions){
        Message.obtain(mHandler, HandlerCode.MAPS_ADD_POLYGON, polygonOptions).sendToTarget();
    }

    private void addCircle(CircleOptions circleOptions){
        Message.obtain(mHandler, HandlerCode.MAPS_ADD_CIRCLE, circleOptions).sendToTarget();
    }

    private void addMarker(MarkerOptions markerOptions){
        Message.obtain(mHandler, HandlerCode.MAPS_ADD_MARKER, markerOptions).sendToTarget();
    }

}
