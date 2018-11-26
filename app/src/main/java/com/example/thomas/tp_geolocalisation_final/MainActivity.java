package com.example.thomas.tp_geolocalisation_final;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //éléments visuels
    private TextView print_loc;
    private Button start;
    private Button stop;
    private TextView print_address;
    private Button get_address;
    private TextView print_distance;

    //verrou pour bouton START et STOP
    private boolean started;

    private double distance_parcourue;

    //requête de localisation
    private LocationRequest mLocationRequest;
    //service client de localisation
    private FusedLocationProviderClient myFusedLocationClient;
    //objet callback
    private LocationCallback myLocationCallback;

    //permet la récupération d'une adresse postale
    private Geocoder geocoder;

    //intervalle de mise à jour
    private long UPDATE_INTERVAL = 3 * 1000;  /* 3 secs */
    //intervalle de mise à jour la plus rapide
    private long FASTEST_INTERVAL = 1000; /* 1 sec */

    //permission de localisation
    private String permissionLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    //private String permissionLocation = Manifest.permission.ACCESS_COARSE_LOCATION;

    //dernière localisation
    private Location lastLocation;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        print_loc = (TextView) findViewById(R.id.print_loc);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        get_address = (Button) findViewById(R.id.get_address);
        print_address = (TextView) findViewById(R.id.print_address);
        print_distance = (TextView) findViewById(R.id.print_distance);

        distance_parcourue = 0;

        lastLocation = null;

        myFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        myLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        geocoder = new Geocoder(this, Locale.getDefault());

        started = false;

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!started) {
                    started = true;
                    distance_parcourue = 0;
                    print_loc.setText("START");
                    print_distance.setText("");
                    startLocationUpdates();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (started) {
                    started = false;
                    myFusedLocationClient.removeLocationUpdates(myLocationCallback);
                    print_loc.append("  STOP");
                }
            }
        });

        get_address.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if(lastLocation != null) {
                        List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
                        Address obj = addresses.get(0);

                        String add = obj.getAddressLine(0) + "," + obj.getAdminArea() + "," + obj.getCountryName();
                        print_address.setText(add);
                    } else {
                        print_address.setText("Aucune localisation fournie");
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                }

            }
        });


    }

    //Déclenche une méthode de mise à jour de la position par intervalle
    protected void startLocationUpdates() {

        // Créer une requête de localisation
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ContextCompat.checkSelfPermission(this, this.permissionLocation) == PackageManager.PERMISSION_GRANTED)
            myFusedLocationClient.requestLocationUpdates(mLocationRequest, myLocationCallback, Looper.myLooper());
        else {
            print_loc.setText("permission non autorisée");
            started = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{this.permissionLocation},
                    1);
        }


    }

    //appelé à chaque réception de nouvelle coordonnées
    public void onLocationChanged(Location location) {
        String msg = "lat : " + Double.toString(location.getLatitude()) + ", lng : " + Double.toString(location.getLongitude());
        print_loc.setText(msg);
        if(lastLocation != null)
            distance_parcourue += distance(lastLocation.getLatitude(), location.getLatitude(), lastLocation.getLongitude(), location.getLongitude());
        print_distance.setText("distance parcourue (en mètres) : "+distance_parcourue);
        lastLocation = location;
    }


    /**
     * Calculate distance between two points in latitude and longitude.
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }

}