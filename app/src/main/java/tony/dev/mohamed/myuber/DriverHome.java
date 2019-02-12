package tony.dev.mohamed.myuber;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.Retrofit.IGoogleApiClient;
import tony.dev.mohamed.myuber.models.UberDriver;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static tony.dev.mohamed.myuber.Retrofit.Common.currentLocation;
import static tony.dev.mohamed.myuber.Retrofit.Common.driver_location_tbl;

public class DriverHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;
    private static final int RC_PHOTO_PICKER = 2;
    private static final int REQUEST_CHECK_SETTINGS = 555;
    //presence system
    DatabaseReference onlineRef, currentUsersRef;
    FirebaseStorage mStorage;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    //private Location currentLocation;
    private boolean firstTimeFlag = true;
    private MaterialAnimatedSwitch locationSwitch;
    private TextView switchText;
    private DatabaseReference mDrivers;
    private GeoFire geoFire;
    private SpotsDialog dialog;
    // car animation
    private CircleImageView driverImage;
    private View view;
    private List<LatLng> ployLineLong;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler mHandler;
    private int index, next;
    private FirebaseAuth mAuth;
    private SupportMapFragment supportMapFragment;
    private LatLng startPosition, endPosition, currentPosition;
    Runnable drawPathRunnbale = new Runnable() {
        @Override
        public void run() {
            if (index < ployLineLong.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < ployLineLong.size() - 1) {
                startPosition = ployLineLong.get(index);
                endPosition = ployLineLong.get(index);
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    googleMap.moveCamera((CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(newPos)
                            .zoom(15.5f)
                            .build()
                    )));
                }
            });
            valueAnimator.start();
            mHandler.postDelayed(this, 3000);

            /*if (index != ployLineLong.size() - 1) {
                mHandler.postDelayed(this, 3000);
            }*/
        }
    };
    private Uri selecteImage = null;
    private AppCompatButton btnGo;
    /* private AppCompatEditText editPlace;*/
    private PlaceAutocompleteFragment places;
    private AutocompleteFilter autocompleteFilter;

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(final LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();
            if (firstTimeFlag && googleMap != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
            }
            //  showMarker(currentLocation);
            if (locationSwitch.isChecked()) {


                currentLocation = locationResult.getLastLocation();
                //create new latlng object for distance
                LatLng center = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mDrivers = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl)/*.child("UberX")*/.child(Common.uberDriver.getVechleType());
                geoFire = new GeoFire(mDrivers);
                //distance in meters
                //heading 0 is  northSide,90 is east , 180 is south and 270 is west
                //based on cmpact
                LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

                LatLngBounds bounds = LatLngBounds.builder()
                        .include(northSide)
                        .include(southSide)
                        .build();

                places.setBoundsBias(bounds);
                places.setFilter(autocompleteFilter);

                /*destinationPlace.setBoundsBias(bounds);
                destinationPlace.setFilter(autocompleteFilter);*/


                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (currentLocationMarker != null) {
                                currentLocationMarker.remove();
                            }
                            // currentUsersRef.onDisconnect().removeValue();
                            currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                    .position(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude())).title("You"));

                            LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(16).build()));
                            // rotateMarker(currentLocationMarker,-360,googleMap);

                        }
                    });
                }
            }
        }
    };
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;
    private IGoogleApiClient googleApiClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees((Math.atan(lng / lat))));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (90 - Math.toDegrees((Math.atan(lng / lat))) + 90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees((Math.atan(lng / lat))) + 180);
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (90 - Math.toDegrees((Math.atan(lng / lat))) + 270);
        else return -1;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        /**
         * setting the toolbar
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mStorage = FirebaseStorage.getInstance();

        /***
         * init the navigation drawer layout
         * and creating action for toggle button
         */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //attatch drawer listenner to the toggle action
        drawer.addDrawerListener(toggle);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
        //finding the navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //attach the listener of item clicked to the navigation view
        navigationView.setNavigationItemSelectedListener(this);
        mAuth = FirebaseAuth.getInstance();
        googleApiClient = Common.getGoogleApi();
        /**
         * geo fire get latlng from geo fire that saved in the database
         */
        //if(uberDriver!=null) {
        /*mDrivers = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl)*//*.child("UberX")*//*.child(Common.uberDriver.getVechleType());
            geoFire = new GeoFire(mDrivers);*/
        // }
        //get driver information
        view = navigationView.getHeaderView(0);
        driverImage = view.findViewById(R.id.user_imageView);
        final TextView rateStars = view.findViewById(R.id.star_text);
        final TextView userName = view.findViewById(R.id.driver_name);
        //  if(mAuth.getCurrentUser()!=null){
        if (mAuth.getCurrentUser() != null) {
            /**
             * check if gbs is open or not , if not
             * will open setting to open it
             */
       /*     final AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
            myBuilder.setMessage("Please Open the gps !");
            myBuilder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            myBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    checkGps();
                }
            });*/
            /**
             * checking network availabilty
             */
            new AsyncTask<Void, Void, String>() {
                @SuppressLint("WrongThread")
                @Override
                protected String doInBackground(Void... voids) {
                    String s = null;
                    if (isNetworkAvailable()) {
                        if (isOnline()) {
                            s = " connected and online ";
                        } else {
                            s = " connected but not online ";
                        }
                    } else {
                        s = "no access";
                    }
                    return s;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if(s.equals("no access")){

                       /* WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null) {
                            wifiManager.setWifiEnabled(true);
                        }*/
                       // wifiManager.setWifiEnabled(false);
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                   // Toast.makeText(DriverHome.this, s, Toast.LENGTH_SHORT).show();
                    Log.e("status n ", s);
                }
            }.execute();


            // mAuth = FirebaseAuth.getInstance();

            //findViewById(R.id.currentLocationImageButton).setOnClickListener(clickListener);
            // init view
            locationSwitch = findViewById(R.id.location_switch);
            switchText=findViewById(R.id.switchText);
            locationSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(boolean isOnline) {
                    if (isOnline) {
                        switchText.setText("online");
                        switchText.setTextColor(getColor(R.color.colorAccent));
                        if (isGpsOpend()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(DriverHome.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                    return;
                                }
                            }
                            if (isGooglePlayServicesAvailable()) {
                                FirebaseDatabase.getInstance().goOnline();
                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DriverHome.this);
                                startCurrentLocationUpdates();
                               // fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DriverHome.this);

                               // Toast.makeText(DriverHome.this, " online", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            checktheGps();
                        }
                    } else {
                        switchText.setText("off line");
                        switchText.setTextColor(getColor(R.color.colorPrimary));
                        if (fusedLocationProviderClient != null)
                            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

                        mDrivers = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl)/*.child("UberX")*/.child(Common.uberDriver.getVechleType());
                        geoFire = new GeoFire(mDrivers);

                        FirebaseDatabase.getInstance().goOffline();
                        if (currentLocationMarker != null)
                            currentLocationMarker.remove();
                        googleMap.clear();
                    /*if(mHandler!=null)
                        mHandler.removeCallbacks(drawPathRunnbale);*/
                    }
                }
            });

            ployLineLong = new ArrayList<>();

            places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplet_fragemednt);
            // to restrict search to city
            autocompleteFilter = new AutocompleteFilter.Builder()
                    .setCountry("eg")
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(3)
                    .build();

            places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    if (locationSwitch.isChecked()) {

                        destination = Objects.requireNonNull(place.getAddress()).toString();
                        destination = destination.replace(" ", "+");
                        getDirection();
                    } else {
                        Toast.makeText(DriverHome.this, "Change your status to online", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Status status) {
                    Toast.makeText(DriverHome.this, "error " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                }
            });


            updateFirebaseToken();


            FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UberDriver uberDriver = dataSnapshot.getValue(UberDriver.class);
                    if (uberDriver != null) {
                        //  Picasso.get().load(uberDriver.getPhoto()).resize(150,150).centerCrop().into(driverImage);
                        Glide.with(getApplicationContext()).load(uberDriver.getPhoto()).apply(new RequestOptions().override(150, 150)).into(driverImage);
                        userName.setText(uberDriver.getName());
                        rateStars.setText(uberDriver.getRates());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Intent intentb = new Intent(DriverHome.this, MainActivity.class);
            startActivity(intentb);
            finish();
        }


    }

    //to check gps is open or not
    private boolean isGpsOpend() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }

    private void updateFirebaseToken() {
        if (mAuth.getCurrentUser() != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (task.isSuccessful()) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                           // Toast.makeText(DriverHome.this, " updated success ", Toast.LENGTH_SHORT).show();
                            reference.child(Common.tokens_tb).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(task.getResult().getToken());
                        }
                        String id = task.getResult().getId();
                        Log.d("user_id", id);
                        String token = task.getResult().getToken();
                        Log.d("user_token", token);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("error_in_token", e.getMessage());
                }
            });
        }
    }

    private void getDirection() {
        currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        String requestApi = null;
       // String apiKey = Arrays.toString(Base64.decode(getGoogleDirectionApi(), Base64.DEFAULT));

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getString(R.string.google_direction_api);
            Log.e("the_url", requestApi);
            googleApiClient.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            ployLineLong = decodePoly(polyline);

                            //adjusting bounds
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (LatLng latLng : ployLineLong) {
                                builder.include(latLng);
                            }
                            LatLngBounds bounds = builder.build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);

                            polylineOptions = new PolylineOptions();
                            polylineOptions.color(Color.GRAY);
                            polylineOptions.width(5);
                            polylineOptions.startCap(new SquareCap());
                            polylineOptions.endCap(new SquareCap());
                            polylineOptions.jointType(JointType.ROUND);
                            polylineOptions.addAll(ployLineLong);
                            greyPolyline = googleMap.addPolyline(polylineOptions);

                            blackPolylineOptions = new PolylineOptions();
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.width(5);
                            blackPolylineOptions.startCap(new SquareCap());
                            blackPolylineOptions.endCap(new SquareCap());
                            blackPolylineOptions.jointType(JointType.ROUND);
                            blackPolylineOptions.addAll(ployLineLong);
                            blackPolyline = googleMap.addPolyline(blackPolylineOptions);

                            googleMap.addMarker(new MarkerOptions().position(ployLineLong.get(ployLineLong.size() - 1))
                                    .title("Pickup Location")
                            );

                            //Annimation
                            ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                            polyLineAnimator.setDuration(2000);
                            polyLineAnimator.setInterpolator(new LinearInterpolator());
                            polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    List<LatLng> pionts = greyPolyline.getPoints();
                                    int percentValue = (int) valueAnimator.getAnimatedValue();
                                    int size = pionts.size();
                                    int newPionts = (int) (size * (percentValue / 100.0f));
                                    List<LatLng> p = pionts.subList(0, newPionts);
                                    blackPolyline.setPoints(p);
                                }
                            });
                            polyLineAnimator.start();

                            carMarker = googleMap.addMarker(new MarkerOptions().position(currentPosition).flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            mHandler = new Handler();
                            index = -1;
                            next = 1;
                            mHandler.postDelayed(drawPathRunnbale, 3000);


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void checktheGps() {
                   /* //  if(mAuth.getCurrentUser()!=null){
                    if (mAuth.getCurrentUser() != null) {
                        *//**
         * check if gbs is open or not , if not
         * will open setting to open it
         *//*
                        final AlertDialog.Builder myBuilder = new AlertDialog.Builder(Home.this);
                        myBuilder.setMessage("Please Open the gps !");
                        myBuilder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        myBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                *//**
         * method for
         * check if gbs is open or not , if not
         * will open setting to open it
         *//*

                                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }

                            }
                        });
                        myBuilder.show();*/


        //}

                    /*GoogleApiClient googleApiClient = new GoogleApiClient.Builder(Home.this)
                            .addApi(LocationServices.API).build();
                    googleApiClient.connect();

                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(10000 / 2);

                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);

                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    Log.i(TAG, "All location settings are satisfied.");
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the result
                                        // in onActivityResult().
                                        status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                                    break;
                            }
                        }
                    });*/

        //================================================================================================

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        // .addLocationRequest(mLocationRequestBalancedPowerAccuracy);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(DriverHome.this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    //...
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        DriverHome.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            // ...
                            break;
                    }
                }
            }
        });
        //================================================================================================
    }

    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DriverHome.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
       // Toast.makeText(DriverHome.this, " online and", Toast.LENGTH_SHORT).show();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0&&grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCurrentLocationUpdates();
        }
    }

    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));

        Log.d("latlng", Double.toString(location.getLatitude()) + "==" + Double.toString(location.getLongitude()));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(12).build();
    }

    private void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null) {
            // currentLocationMarker = googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng));
            currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).position(latLng).title("You"));
            // rotateMarker(currentLocationMarker, -360, googleMap);
        } else
            MarkerAnimation.animateMarkerToGB(currentLocationMarker, latLng, new LatLngInterpolator.Spherical());
    }

    /**
     * method for
     * check if gbs is open or not , if not
     * will open setting to open it
     */
/*    private void checkGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }*/

    /**
     * check if the uberDriver open the netwoke provider(wifi of mobile data)
     *
     * @return ture or fasle
     */
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * check if the newtwork is online by making ping to google website
     *
     * @return ture or fasle
     */
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

/*    @Override
    protected void onDestroy() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        mDrivers = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl)*//*.child("UberX")*//*.child(Common.uberDriver.getVechleType());
        geoFire = new GeoFire(mDrivers);

        FirebaseDatabase.getInstance().goOffline();
        if (currentLocationMarker != null)
            currentLocationMarker.remove();
        googleMap.clear();
        if (mHandler != null)
            mHandler.removeCallbacks(drawPathRunnbale);
        super.onDestroy();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            //startCurrentLocationUpdates();
        }
        //presence system
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
            currentUsersRef = FirebaseDatabase.getInstance().getReference().child(driver_location_tbl)
                    .child(Common.uberDriver.getVechleType())
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            currentUsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currentUsersRef.onDisconnect().removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            fusedLocationProviderClient = null;
            googleMap = null;
        }

        mDrivers = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl)/*.child("UberX")*/.child(Common.uberDriver.getVechleType());
        geoFire = new GeoFire(mDrivers);

        FirebaseDatabase.getInstance().goOffline();
        if (currentLocationMarker != null)
            currentLocationMarker.remove();
        if (googleMap != null)
            googleMap.clear();
        if (mHandler != null)
            mHandler.removeCallbacks(drawPathRunnbale);


        super.onDestroy();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_trip_history) {
            // Handle the camera action
        } else if (id == R.id.nav_way_bill) {

        } else if (id == R.id.nav_car_type) {
            showCarTypeDialog();

        } else if (id == R.id.nav_change_paasword) {
            showChangePasswordtDialog();
        } else if (id == R.id.nav_change_information) {
            showChangeInformationDialog();
        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_signout) {
            signOut();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showCarTypeDialog() {
        //creating aleart dialog builder object
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting cancelable to false
        builder.setCancelable(false);
        //set title
        builder.setTitle("Select Car Type");
        /**
         * getting layoutinflater object
         *
         */
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //convert xml file into corespondings views objects
        final View view = inflater.inflate(R.layout.layout_update_vechel, null);
        //intit radio button for UberX car type
        final RadioButton uberXbtn = view.findViewById(R.id.radio_uberx);
        //intit radio button for Uber Black car type
        final RadioButton uberBlack = view.findViewById(R.id.radio_uber_black);

        if (Common.uberDriver.getVechleType().equals("UberX")) {
            uberXbtn.setChecked(true);
        } else if (Common.uberDriver.getVechleType().equals("Uber Black")) {
            uberBlack.setChecked(true);
        }


        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //hide the dailog
                dialogInterface.dismiss();
            }
        });
        //seeting reset button
        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //checking if the email is empty
                final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    Map map = new HashMap();
                    if (uberXbtn.isChecked()) {
                        map.put("vechleType", uberXbtn.getText().toString());
                    } else if (uberBlack.isChecked()) {
                        map.put("vechleType", uberBlack.getText().toString());
                    }

                    FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(mUser.getUid()).updateChildren(map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                currentUsersRef = FirebaseDatabase.getInstance().getReference().child(driver_location_tbl)
                                        .child(Common.uberDriver.getVechleType())
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                Toast.makeText(DriverHome.this, " car type changed successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(DriverHome.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        });
        //set the custom view
        builder.setView(view);
        //show the dialog
        builder.show();
    }

    private void showChangeInformationDialog() {
        //creating aleart dialog builder object
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting cancelable to false
        builder.setCancelable(false);
        //set title
        builder.setTitle("CHANGE INFORMATION");
        //set message
        builder.setMessage("Please Enter All Required Fields !");
        /**
         * getting layoutinflater object
         *
         */
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //convert xml file into corespondings views objects
        final View view = inflater.inflate(R.layout.layout_update_information, null);
        //intialize old pass edit text
        final AppCompatEditText user_name = view.findViewById(R.id.name_edit_info);
        //intialize new pass edit text
        final AppCompatEditText user_emial = view.findViewById(R.id.email_edit_ifo);
        final AppCompatEditText user_phone = view.findViewById(R.id.phone_edit_ifo);
        final LinearLayout linearLayout = view.findViewById(R.id.linear_layout);
        final ProgressBar progressBar = view.findViewById(R.id.progress_circular);
        final ImageButton userImage = view.findViewById(R.id.add_user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        //setting cancel button
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        //getting information
        if (mUser != null) {
            FirebaseDatabase mUserDatabase = FirebaseDatabase.getInstance();
            mUserDatabase.getReference(Common.user_driver_tbl).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UberDriver uberDriver = dataSnapshot.getValue(UberDriver.class);
                    // String name = dataSnapshot.child("name").getValue().toString();
                    progressBar.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);

                    user_name.setText(Objects.requireNonNull(uberDriver).getName());
                    user_phone.setText(uberDriver.getPhone());
                    // user_name.setText(mUser.getDisplayName());
                    user_emial.setText(mUser.getEmail());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //hide the dailog
                dialogInterface.dismiss();
            }
        });
        //seeting reset button
        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //showing spots dialog
                dialog = new SpotsDialog(DriverHome.this, "Please Wait...");
                dialog.show();
                //getting old pass form uberDriver
                final String name = user_name.getText().toString();
                //getting old pass form uberDriver
                final String email = user_emial.getText().toString();
                final String phone = user_phone.getText().toString();
                //checking if the email is empty
                final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    Map map = new HashMap();
                    map.put("name", name);
                    map.put("email", email);
                    map.put("phone", phone);
                    FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(mUser.getUid()).updateChildren(map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            dialog.dismiss();
                                            Toast.makeText(DriverHome.this, " information updated successfully!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(DriverHome.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        });
        //set the custom view
        builder.setView(view);
        //show the dialog
        builder.show();
    }

    //method for aleart dialog to reset password
    private void showChangePasswordtDialog() {
        //creating aleart dialog builder object
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting cancelable to false
        builder.setCancelable(false);
        //set title
        builder.setTitle("CHANGE PASSWORD");
        //set message
        builder.setMessage("Please Enter All Required Fields !");
        /**
         * getting layoutinflater object
         *
         */
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //convert xml file into corespondings views objects
        final View view = inflater.inflate(R.layout.change_password, null);
        //intialize old pass edit text
        final AppCompatEditText oldPass = view.findViewById(R.id.old_pass_edit);
        //intialize new pass edit text
        final AppCompatEditText newPass = view.findViewById(R.id.new_pass_edit);
        //intialize repeat pass edit text
        final AppCompatEditText newRepeatPass = view.findViewById(R.id.repeat_pass_edit);
        //setting cancel button
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //hide the dailog
                dialogInterface.dismiss();
            }
        });
        //seeting reset button
        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //showing spots dialog
                final SpotsDialog dialog = new SpotsDialog(DriverHome.this, "Please Wait...");
                dialog.show();
                //getting old pass form uberDriver
                String mOldPass = oldPass.getText().toString();
                //getting old pass form uberDriver
                final String mNewPass = newPass.getText().toString();
                //getting old pass form uberDriver
                final String mRepeatPass = newRepeatPass.getText().toString();
                //checking if the email is empty
                if (TextUtils.isEmpty(mOldPass) && TextUtils.isEmpty(mNewPass) && TextUtils.isEmpty(mRepeatPass)) {
                    dialog.dismiss();
                    showChangePasswordtDialog();
                    Snackbar.make(supportMapFragment.getView(), "Please Enter All Fields !", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(mOldPass)) {
                    dialog.dismiss();
                    showChangePasswordtDialog();
                    Snackbar.make(supportMapFragment.getView(), "Please Enter Your old Password", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(mNewPass)) {
                    dialog.dismiss();
                    showChangePasswordtDialog();
                    Snackbar.make(supportMapFragment.getView(), "Please Enter Your new Password", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(mRepeatPass)) {
                    dialog.dismiss();
                    showChangePasswordtDialog();
                    Snackbar.make(supportMapFragment.getView(), "Please Repeat Your new Password", Snackbar.LENGTH_LONG).show();
                } else {
                    if (mNewPass.equals(mRepeatPass)) {
                        final FirebaseUser user = mAuth.getCurrentUser();
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), mOldPass);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(mNewPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                                Toast.makeText(DriverHome.this, "password updated successfully!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(DriverHome.this, "" + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    dialog.dismiss();
                                    //  Toast.makeText(DriverHome.this, ""+task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(DriverHome.this, "The old password is incorrect!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(DriverHome.this, "passwoed doesn't match", Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });
        //set the custom view
        builder.setView(view);
        //show the dialog
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                //mStorage.getReference().child("driver_images").child(mAuth.getCurrentUser().getUid()).
                selecteImage = data.getData();
                if (selecteImage != null) {
                    dialog = new SpotsDialog(DriverHome.this, "Please Wait...");
                    dialog.show();
                    uploadFile(selecteImage);
                }
            }
        }
    }

    private void uploadFile(Uri selectedImageUri) {
        //these three line of code only for to give the uploaded file name
        //File f = new File(String.valueOf(selectedImageUri.getLastPathSegment()));
        //String imageName = f.getName();
        String imageName = UUID.randomUUID().toString();
        StorageReference mStorageRefrence = mStorage.getReference().child("driver_images");
        final StorageReference storageReference = mStorageRefrence.child(imageName);
        storageReference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(view).load(uri).apply(new RequestOptions().override(150, 150)).into(driverImage);

                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            Map map = new HashMap();
                            map.put("photo", uri.toString());
                            FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(mAuth.getCurrentUser().getUid()).updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        dialog.dismiss();
                                        Toast.makeText(DriverHome.this, " information updated successfully!", Toast.LENGTH_SHORT).show();

                                    } else {
                                        dialog.dismiss();
                                        Toast.makeText(DriverHome.this, " uploading fialed !", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                        }

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                dialog.setMessage("uploading " + (int) progress + "%");

            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(DriverHome.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
       /* .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        })*/

    }

    private void signOut() {
        Log.d("in_sign_out", "in_sign_out");
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Intent intent = new Intent(DriverHome.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d("out_sign_out", "in_sign_out");
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

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.setTrafficEnabled(false);
        this.googleMap.setIndoorEnabled(false);
        this.googleMap.setBuildingsEnabled(false);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
