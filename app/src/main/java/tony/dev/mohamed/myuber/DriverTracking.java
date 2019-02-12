package tony.dev.mohamed.myuber;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuber.FirebaseService.IFCMService;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.Retrofit.IGoogleApiClient;
import tony.dev.mohamed.myuber.models.DataMessage;
import tony.dev.mohamed.myuber.models.DirectionsJSONParser;
import tony.dev.mohamed.myuber.models.FCMResponse;
import tony.dev.mohamed.myuber.models.Token;

import static tony.dev.mohamed.myuber.Retrofit.Common.currentLocation;
import static tony.dev.mohamed.myuber.Retrofit.Common.uberDriver;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static int MY_PERMISSION_REQUEST_CODE = 7000;
    private static int PLAY_SERVICE_RES_REQUEST = 7001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLAY_CREMENT = 10;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private IGoogleApiClient iGoogleApiClient;
    private String riderLat;
    private String riderLng;
    private SupportMapFragment mapFragment;
    private Circle riderMarker;
    private Marker driverMarker;
    //for routs between to locations
    private Polyline direction;
    private LatLng startPosition, endPosition, currentPosition;

    private IFCMService ifcmService;
    private GeoFire geoFire;
    private String customerId;
    private Button btnStartTrip;
    private Location pickupLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent() != null) {
            riderLat = getIntent().getStringExtra("lat");
            riderLng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customerId");

        }
        iGoogleApiClient = Common.getGoogleApi();
        ifcmService = Common.getFcmService();

        setUpLocation();
        btnStartTrip = findViewById(R.id.startTripe);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnStartTrip.getText().toString().equalsIgnoreCase("Start Trip")) {
                    pickupLocation = Common.currentLocation;
                    btnStartTrip.setText("DROP OFF HERE");
                }else if(btnStartTrip.getText().toString().equalsIgnoreCase("DROP OFF HERE")){
                    calculateFees(pickupLocation,Common.currentLocation);
                }
            }
        });

    }

    private void calculateFees(final Location pickupLocation, Location currentLocation) {
      //  currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        String requestApi = null;

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + pickupLocation.getLatitude() + "," + pickupLocation.getLongitude() + "&" +
                    "destination=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&" +
                    "key=" + getString(R.string.google_direction_api);
            Log.e("the_url", requestApi);
            iGoogleApiClient.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        //
                        if (response.body() != null) {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            JSONArray routes = jsonObject.getJSONArray("routes");
                            JSONObject jsonObject1 = routes.getJSONObject(0);
                            JSONArray legs = jsonObject1.getJSONArray("legs");
                            JSONObject legsObject = legs.getJSONObject(0);
                            //get distance
                            JSONObject distance = legsObject.getJSONObject("distance");
                            String distance_text=distance.getString("text");
                            //use regex to extract double from string
                            //this regex will remove all text isn't digit
                            Double distance_value=Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]",""));
                            //get duration
                            JSONObject duration = legsObject.getJSONObject("duration");
                            String time_text=duration.getString("text");
                            Integer time_value=Integer.parseInt(time_text.replaceAll("\\D+",""));
                            //get address
                            String endAddress=legsObject.getString("end_address");
                            Log.d("end_address",endAddress);

                            String final_calculation=String.format("%s + %s = $%.2f",distance_text,time_text,
                                    Common.getPrice(distance_value,time_value)
                            );
                            Toast.makeText(DriverTracking.this, " drop me is called", Toast.LENGTH_SHORT).show();

                            sendDropOffNotification(customerId);

                            Intent intent=new Intent(DriverTracking.this,TripDetail.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            Log.d("Start_Address",legsObject.getString("start_address"));
                            intent.putExtra("start_address",legsObject.getString("start_address"));
                            intent.putExtra("endAddress",endAddress);
                            intent.putExtra("time",String.valueOf(time_value));
                            intent.putExtra("distance",String.valueOf(distance_value));
                            intent.putExtra("total",String.format("$ %.2f",Common.getPrice(distance_value,time_value)));
                            intent.putExtra("location_start",String.format("%f,%f",pickupLocation.getLatitude(),pickupLocation.getLongitude()));
                            intent.putExtra("location_end",String.format("%f,%f",Common.currentLocation.getLatitude(),Common.currentLocation.getLongitude()));
                            /*DriverTracking.this.*/startActivity(intent);


                        } else {
                            Log.d("response_body", "null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });

            new ParseTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccessful = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map));
            if (!isSuccessful) {
                Log.e("Error", "eroro");
            }
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        riderMarker = mMap.addCircle(new CircleOptions().
                center(new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLng))).
                radius(50).strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)
        );

        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl).child(Common.uberDriver.getVechleType()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat), Double.parseDouble(riderLng)), 0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArriveNotification(customerId);
               // btnStartTrip.setEnabled(true);
               // btnStartTrip.setText("DROP OFF HERE");
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendArriveNotification(String customerId) {
        Token token = new Token(customerId);
        /*Notification notification = new Notification("Arrived", String.format("the driver %s has arrived at your location", uberDriver.getName()));
        Sender sender = new Sender(notification, token.getToken());*/
        HashMap<String,String> content=new HashMap<>();
        content.put("title","Arrived");
        content.put("message",String.format("the driver %s has arrived at your location", uberDriver.getName()));
        DataMessage dataMessage=new DataMessage(token.getToken(),content);
        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }
    private void sendDropOffNotification(String customerId) {
        Token token = new Token(customerId);
        /*Notification notification = new Notification("DropOff",customerId);
        Sender sender = new Sender(notification, token.getToken());*/
       Map<String,String> content=new HashMap<>();
        content.put("title","DropOff");
        content.put("message",customerId);
        DataMessage dataMessage=new DataMessage(token.getToken(),content);
        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        //checkLocationPermision();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the uberDriver grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the uberDriver grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Common.currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.currentLocation != null) {

            final double mLatitude = Common.currentLocation.getLatitude();
            final double mLongtude = Common.currentLocation.getLongitude();
            Log.d("lonlat", Double.toString(mLatitude) + " " + Double.toString(mLongtude));
            if (driverMarker != null) {
                driverMarker.remove();
            }
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mLatitude, mLongtude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .title("You"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongtude), 17.0f));
            if (direction != null)
                direction.remove();
            getDirection();
        }
       /* }else{
            Log.e("Error","Can't get location");
        }*/
    }

    private void getDirection() {
        currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        String requestApi = null;

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + riderLat + "," + riderLng + "&" +
                    "key=" + getString(R.string.google_direction_api);
            Log.e("the_url", requestApi);
            iGoogleApiClient.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        if (response.body() != null) {
                            Log.d("response_body", response.body().toString());
                            new ParseTask().execute(response.body().toString());
                        } else {
                            Log.d("response_body", "null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });

            new ParseTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }
        } else {
            if (checkPlayservices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }
    }

    private void createLocationRequest() {
       /* mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLAY_CREMENT);*/

        //new lcode
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLAY_CREMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayservices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Snackbar.make(mapFragment.getView(), "This Device is not supported", Snackbar.LENGTH_LONG).show();
                finish();
            }
            return false;

        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.currentLocation = location;
        displayLocation();
    }

    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog progressDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject object;
            List<List<HashMap<String, String>>> routs = null;
            try {
                object = new JSONObject(strings[0]);
                DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();
                routs = directionsJSONParser.parse(object);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routs;

        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);
//            Log.d("lists",lists.toString());
            progressDialog.dismiss();

            ArrayList pionts = null;
            PolylineOptions options = null;

            if (lists != null) {
                for (int i = 0; i < lists.size(); i++) {
                    pionts = new ArrayList();
                    options = new PolylineOptions();
                    List<HashMap<String, String>> path = lists.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> piont = path.get(j);

                        double lat = Double.parseDouble(piont.get("lat"));
                        double lng = Double.parseDouble(piont.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        pionts.add(position);


                    }
                    options.addAll(pionts);
                    options.width(10);
                    options.color(Color.RED);
                    options.geodesic(true);
                }
                direction = mMap.addPolyline(options);
            } else {
                Log.d("my_lists", "null");
            }
        }
    }
}
