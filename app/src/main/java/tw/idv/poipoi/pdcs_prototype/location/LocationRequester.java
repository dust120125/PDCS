package tw.idv.poipoi.pdcs_prototype.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by DuST on 2017/5/18.
 */

public class LocationRequester implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private final Context appContext;

    private GoogleApiClient googleApiClient;

    private LocationRequest locationRequest;
    private Location currentLocation;
    private boolean mhasLocationService = false;

    private static final long REQUEST_INTERVAL = 60000 * 5;
    private static final long REQUEST_FASTEST_INTERVAL = 60000 * 5;

    private List<LocationListener> locationListeners = new LinkedList<>();

    public LocationRequester(Context context) {
        this.appContext = context;
        configGoogleApiClient(context);
        configLocationRequest();
        googleApiClient.connect();
    }

    private synchronized void configGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(REQUEST_INTERVAL)
                .setFastestInterval(REQUEST_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
    }

    public void removeLocationListener(LocationListener listener){
        locationListeners.remove(listener);
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public boolean hasLocationService() {
        return mhasLocationService;
    }

    public boolean requestLocationService(){
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null){
            onLocationChanged(currentLocation);
        }
        mhasLocationService = true;
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // 已經連線到Google Services
        requestLocationService();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        // int參數是連線中斷的代號
        Log.d("GoogleService", "onConnectionSuspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(appContext, com.google.android.gms.R.string.common_google_play_services_unsupported_text,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        for (LocationListener listener : locationListeners) {
            if (listener != null)
                listener.onLocationChanged(location);
        }
    }
}
