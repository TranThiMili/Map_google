package com.example.mili.demo_googlepru;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout lnMienBac, lnMienNam, lnMienTrung;
    View viewMienBac, viewMienNam, viewMienTrung;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ListView lvMap;
    private ProgressDialog progressDialog;
    private static final int REQUEST_LOCATION = 9999;
    private ArrayList<Office> arrayList1 = new ArrayList<>();
    private AdapterOffice adapterOffice;
    private Location myLocation = null;
    GoogleMap mMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        control();
        loadGoogleMap();
        event();
    }

    //Anh xa
    private void control() {
        lvMap = findViewById(R.id.lvmap);
        lnMienBac = findViewById(R.id.lnNorth);
        lnMienNam = findViewById(R.id.lnSouth);
        lnMienTrung = findViewById(R.id.lnMiddle);
        viewMienBac = findViewById(R.id.viewNorth);
        viewMienNam = findViewById(R.id.viewSouth);
        viewMienTrung = findViewById(R.id.viewMiddle);
    }


    private void event() {
        lnMienTrung.setOnClickListener(this);
        lnMienNam.setOnClickListener(this);
        lnMienBac.setOnClickListener(this);

        lvMap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    mMap.clear();
                    //displayMyLocation(myLocation);
                    if (arrayList1.get(position)._mlagn != null && !arrayList1.get(position)._mlong.equals("") &&
                            arrayList1.get(position)._mlagn != null && !arrayList1.get(position)._mlagn.equals("")) {

                        LatLng destFrom = new LatLng(Double.parseDouble(arrayList1.get(position)._mlagn), Double.parseDouble(arrayList1.get(position)._mlong));
//                        if (myLocation != null) {
//                            LatLng destTo = new LatLng(Double.parseDouble(myLocation.getLatitude() + ""), Double.parseDouble(myLocation.getLongitude() + ""));
//                            showDiretion(destTo, destFrom, "", arrayList1.get(position)._mName);
//                        }
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(destFrom)
                                .title(arrayList1.get(position)._mName)
                                .snippet(arrayList1.get(position)._mAdress));

                        selectedMarker(destFrom, marker);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void selectedMarker(LatLng location, Marker marker) {
        if (location != null && marker != null) {
            int height = 35;
            int width = 35;
            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.grab);
            Bitmap b=bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            marker.showInfoWindow();
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        }
    }

    private void loadGoogleMap() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frgmap);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    onMyMapReady(googleMap);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMyMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mMap = googleMap;// Lấy đối tượng Google Map ra:
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Thông Báo");
            progressDialog.setMessage("Đang tải GoogleMap");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            // Thiết lập sự kiện đã tải Map thành công
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    // Đã tải thành công thì tắt Dialog Progress đi
                    progressDialog.dismiss();
                    initPermission();
                }
            });

            if (!(Build.VERSION.SDK_INT > Build.VERSION_CODES.M)) {
                progressDialog.dismiss();
            }
        }
    }

    //Cấp quyền
    public void initPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //--------------Location - Bluetoooth
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //Permisson don't granted
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        Log.e("MAP", "Permission isn't granted");
                    }
                    // Permisson don't granted and dont show dialog again.
                    else {
                        Log.e("MAP", "Permisson don't granted and dont show dialog again");
                    }
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, REQUEST_LOCATION);
                } else {
                    // Hiển thị vị trí hiện thời trên bản đồ khi đã cấp quyền
                    if (mMap != null) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        //map.getUiSettings().setZoomControlsEnabled(true);
                        mMap.setMyLocationEnabled(true);
                    }
                    showMyLocation();
                }
            } else {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, "Vui lòng bật GPS !", Toast.LENGTH_SHORT).show();
                } else {
                    // Hiển thị vị trí hiện thời trên bản đồ.
                    if (mMap != null) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        //map.getUiSettings().setZoomControlsEnabled(true);
                        mMap.setMyLocationEnabled(true);
                    }
                    showMyLocation();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tìm một nhà cung cấp vị trị hiện thời đang được mở.
    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Tiêu chí để tìm một nhà cung cấp vị trí.
        Criteria criteria = new Criteria();
        // Tìm một nhà cung vị trí hiện thời tốt nhất theo tiêu chí trên.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isEnabledBest = locationManager.isProviderEnabled(bestProvider);// 1 trong 3 trường hợp trên
        if (isGPSEnabled || isNetworkEnabled || isPassiveEnabled || isEnabledBest) {
            if (isPassiveEnabled) {
                return LocationManager.PASSIVE_PROVIDER; //Hiện tại only run case this
            }
        } else {
            Log.e("MAP", "No location provider enabled!");
            return null;
        }
        return bestProvider; //LocationManager.PASSIVE_PROVIDER - LocationManager.GPS_PROVIDER
    }


    //Hiển thị vị trí hiện thời trên bản đồ. - Chỉ gọi phương thức này khi đã có quyền xem vị trí người dùng.
    public void showMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        /*boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);*/

        String locationProvider = this.getEnabledLocationProvider();
        if (locationProvider == null) {
            return;
        }
        // Millisecond
        final long MIN_TIME_BW_UPDATES = 1000;
        // Met
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        try {
            // Đoạn code nay cần người dùng cho phép (Hỏi ở trên ***).
            locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) MainActivity.this);

            // Lấy ra vị trí.
            myLocation = locationManager.getLastKnownLocation(locationProvider);
//
//            if (myLocation == null && mLocationResolver != null) { //API 22 --> lower
//                mLocationResolver.resolveLocation(getActivity(), new LocationResolver.OnLocationResolved() {
//                    @Override
//                    public void onLocationResolved(Location location) {
//                        myLocation = location;
//                        displayMyLocation(location);
//                    }
//                });
//            } else {
                displayMyLocation(myLocation);
            //}

        } catch (SecurityException e) { // Với Android API >= 23 phải catch SecurityException.
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("MAP", "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }
    }



    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void displayMyLocation(Location myLocation) {
        try {
            if (myLocation != null) {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                //mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)             // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                createMarker(myLocation.getLatitude(), myLocation.getLongitude(),"this is me");
                // Thêm Marker cho Map:
                MarkerOptions option = new MarkerOptions();
                option.title("Vị trí hiện tại");
                //option.snippet("....");
                option.icon(BitmapDescriptorFactory.fromResource(R.drawable.grab));
                option.position(latLng);
                Marker currentMarker = mMap.addMarker(option);
                currentMarker.showInfoWindow();
            } else {
                Toast.makeText(this, "Vui lòng bật GPS !", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                    displayMyLocation(myLocation);
                }
                return;
            }
        }
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.setMinZoomPreference(10);
                    return false;
                }
            };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {

                    mMap.setMinZoomPreference(12);

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));

                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);

                    mMap.addCircle(circleOptions);
                }
            };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lnNorth:
                mMap.clear();
                showNorth();
                break;
            case R.id.lnMiddle:
                mMap.clear();
                showCenter();
                break;
            case R.id.lnSouth:
                mMap.clear();
                showSouth();
                break;
            default:
                break;
        }
    }

    //show location North, South, Center
    public void showNorth() {
        try {
            viewMienBac.setVisibility(View.VISIBLE);
            viewMienTrung.setVisibility(View.INVISIBLE);
            viewMienNam.setVisibility(View.INVISIBLE);
            arrayList1 = new ArrayList<>();
            arrayList1.add(new Office("13.75799", "109.2177481", "vintop5", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            arrayList1.add(new Office("13.7648314", "109.2159472", "vintop2", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            adapterOffice = new AdapterOffice(MainActivity.this, R.layout.item_lvgooglemap, arrayList1);
            lvMap.setAdapter(adapterOffice);
            adapterOffice.notifyDataSetChanged();
            for (int i = 0; i < arrayList1.size(); i++) {
                createMarker(Double.parseDouble(arrayList1.get(i)._mlagn), Double.parseDouble(arrayList1.get(i)._mlong), arrayList1.get(i)._mName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showSouth() {
        try {
            viewMienBac.setVisibility(View.INVISIBLE);
            viewMienTrung.setVisibility(View.INVISIBLE);
            viewMienNam.setVisibility(View.VISIBLE);
            arrayList1 = new ArrayList<>();
            arrayList1.add(new Office("13.7713852", "109.2188491", "vintop9", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            arrayList1.add(new Office("13.767973", "109.2160146", "vintop2", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            arrayList1.add(new Office("13.7648314", "109.2159472", "vintop3", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            adapterOffice = new AdapterOffice(MainActivity.this, R.layout.item_lvgooglemap, arrayList1);
            lvMap.setAdapter(adapterOffice);
            adapterOffice.notifyDataSetChanged();
            for (int i = 0; i < arrayList1.size(); i++) {
                createMarker(Double.parseDouble(arrayList1.get(i)._mlagn), Double.parseDouble(arrayList1.get(i)._mlong), arrayList1.get(i)._mName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void showCenter() {
        try {
            viewMienBac.setVisibility(View.INVISIBLE);
            viewMienTrung.setVisibility(View.VISIBLE);
            viewMienNam.setVisibility(View.INVISIBLE);
            arrayList1 = new ArrayList<>();
            arrayList1.add(new Office("13.767973", "109.2160146", "vintop8", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            arrayList1.add(new Office("13.75799", "109.2177481", "vintop2", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            arrayList1.add(new Office("13.7648314", "109.2159472", "vintop3", "23 Ngô Mây-Phường Nguyễn Văn Cừ", "09866666"));
            adapterOffice = new AdapterOffice(MainActivity.this, R.layout.item_lvgooglemap, arrayList1);
            lvMap.setAdapter(adapterOffice);
            adapterOffice.notifyDataSetChanged();
            for (int i = 0; i < arrayList1.size(); i++) {
                createMarker(Double.parseDouble(arrayList1.get(i)._mlagn), Double.parseDouble(arrayList1.get(i)._mlong), arrayList1.get(i)._mName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Direction

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        //Key
        String key = "&key=" + getResources().getString(R.string.api_direction_key);
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + key;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            //Log.e(TAG, "Exception while downloading url : " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    //A class to parse the Google Places in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                //Toast.makeText(activity, "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.RED);
            }

            //Log.e(TAG, "--->" + "Distance:" + distance + ", Duration:" + duration);
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    public void showDiretion(LatLng destTo, LatLng destFrom, String title, String snippet) {
        if (destTo != null && destFrom != null) {
            mMap.clear();
            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(destTo, destFrom);
            //Log.e("Url for Api Diretion: ", url);
            DownloadTask downloadTask = new DownloadTask();
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        } else {
            Toast.makeText(MainActivity.this, "False Points", Toast.LENGTH_SHORT).show();
        }
    }


    protected Marker createMarker(double latitude, double longitude, String title) {
        int height = 35;
        int width = 35;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.grab);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
