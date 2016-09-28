package com.malhotra.nearbyplaces;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by Malhotra G on 7/23/2016.
 */
public class MapMarker extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    GoogleMap googleMap = null;
    SQLiteDatabase db;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Button btnPump,btnHos;
    LatLng latLng;
    String place = "hospital";
    SupportMapFragment mFragment;
    Marker currLocationMarker;
    int drawable = R.drawable.hos;

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setUpMap();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
        buildGoogleApiClient();

        mGoogleApiClient.connect();
    }

    private void setUpMap() {


        //ArrayList<String> data = new ArrayList<>();
        StringBuilder fetch = new StringBuilder("Select * from NEARBY ");
        fetch.append("WHERE Type = \'");
        fetch.append(place+"\'");
        Log.e("tb", fetch.toString());
        try {

            Cursor cur = db.rawQuery(fetch.toString(), null);
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                //Log.e("add","marker");
                MarkerOptions markerOptions = new MarkerOptions();
                Double lat = cur.getDouble(0);
                Double lng = cur.getDouble(1);
                String placeName = cur.getString(2);
                String vicinity = cur.getString(3);
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity);
                String type = cur.getString(4);
                //if(type.equals("hospital"))
                googleMap.addMarker(markerOptions)
                        .setIcon(BitmapDescriptorFactory.fromResource(drawable));
                //googleMap.moveCamera();
                cur.moveToNext();
            }
            cur.close();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);
        btnPump = (Button) findViewById(R.id.btnPump);
        btnHos = (Button) findViewById(R.id.btnHos);

        btnPump.setOnClickListener(this);
        btnHos.setOnClickListener(this);
        db = openOrCreateDatabase("MapData.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        setUpMapIfNeeded();
    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.e("lat",String.valueOf(mLastLocation.getLatitude()));
            Log.e("lng",String.valueOf(mLastLocation.getLongitude()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            //currLocationMarker = googleMap.addMarker(markerOptions);
        }
        //zoom to current position:
        try {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(13).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        catch (Exception e){
            Log.e("camerapos",e.toString());
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        //place marker at current position
        //mGoogleMap.clear();
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        //currLocationMarker = googleMap.addMarker(markerOptions);

        //Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();

//zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()

                .target(latLng).zoom(13).build();

        //If you only need one location, unregister the listener
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btnHos:
                googleMap.clear();
                place = "hospital";
                drawable = R.drawable.hos;
                setUpMap();
                break;
            case R.id.btnPump:
                googleMap.clear();
                place = "gas_station";
                drawable = R.drawable.pump;
                setUpMap();
                break;
        }
    }
}
