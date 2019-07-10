package com.example.gps_courier_2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 982;
    private LocationManager locationManager;
    private LocationListener locationListener;
    TextView tvLocationLatitude;
    TextView tvLocationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocationLatitude = findViewById(R.id.tvLatitude);
        tvLocationLongitude = findViewById(R.id.tvLongitude);
        tvLocationLatitude.setText("Сука шо?");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println("onLocationChanged " + location.getProvider());
                showLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("status " + provider + " " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                System.out.println("enabled " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                System.out.println("disabled " + provider);
            }

        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        } else {
            setUpdateListeners();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (String permission : permissions) {
            System.out.println("Выдача доступов к " + permission);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            setUpdateListeners();
        }
    }

    private void showLocation(Location location) {
        if (location != null) {
            DecimalFormat df = new DecimalFormat("#.######");
            df.setRoundingMode(RoundingMode.CEILING);
//            Toast.makeText(this, df.format(location.getLatitude()) + "\n" + df.format(location.getLongitude()), Toast.LENGTH_LONG).show();
            tvLocationLatitude.setText(df.format(location.getLatitude()));
            tvLocationLongitude.setText(df.format(location.getLongitude()));
            JSONObject json = new JSONObject();
            try {
                System.out.println("time: "+location.getTime());
                json.put("latitude", df.format(location.getLatitude()));
                json.put("longitude", df.format(location.getLongitude()));
                json.put("accuracy", df.format(location.getAccuracy()));
                json.put("provider", location.getProvider());
                json.put("time",location.getTime()/1000);
                new AsyncRequest().execute(json.toString());
            } catch (JSONException e) {
                System.out.println("Что-то не заджсонилось");
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpdateListeners() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
        System.out.println("Привязал слушатель к " + LocationManager.NETWORK_PROVIDER + " и " + LocationManager.GPS_PROVIDER);

    }

    static class AsyncRequest extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            return sendPost(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println("onPostExecute " + s);
        }

        private String sendPost(String params) {
            String urlString = "https://prosto.group/msk/dashboard/Modules/Courier/gps.php"; // URL to call
            OutputStream out;

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(params);
                writer.flush();
                writer.close();
                out.close();
                urlConnection.connect();
                System.out.println(urlConnection.getResponseCode());
                return urlConnection.getResponseMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
    }
}
