package tony.dev.mohamed.myuber;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.Retrofit.IGoogleApiClient;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static tony.dev.mohamed.myuber.Retrofit.Common.currentLocation;
import static tony.dev.mohamed.myuber.Retrofit.Common.driver_location_tbl;
import static tony.dev.mohamed.myuber.Retrofit.Common.user_driver_tbl;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    //private Location currentLocation;
    private boolean firstTimeFlag = true;
    private MaterialAnimatedSwitch locationSwitch;
    private DatabaseReference mDrivers;
    private GeoFire geoFire;

    //presence system
    DatabaseReference onlineRef,currentUsersRef;


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
            showMarker(currentLocation);
            if (locationSwitch.isChecked()) {

                //create new latlng object for distance
                LatLng center=new LatLng(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude());
                //distance in meters
                //heading 0 is  northSide,90 is east , 180 is south and 270 is west
                //based on cmpact
                LatLng northSide=SphericalUtil.computeOffset(center,100000,0);
                LatLng southSide=SphericalUtil.computeOffset(center,100000,180);

                LatLngBounds bounds=LatLngBounds.builder()
                        .include(northSide)
                        .include(southSide)
                        .build();

                places.setBoundsBias(bounds);
                places.setFilter(autocompleteFilter);

                /*destinationPlace.setBoundsBias(bounds);
                destinationPlace.setFilter(autocompleteFilter);*/

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
    };
    // car animation
    private List<LatLng> ployLineLong;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler mHandler;
    private int index, next;
    private LatLng startPosition, endPosition, currentPosition;
    //==========================================================================
    //================================لسه===================================
    //==========================================================================
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
         /*   if (index == 0) {
                BeginJourneyEvent beginJourneyEvent = new BeginJourneyEvent();
                beginJourneyEvent.setBeginLatLng(startPosition);
                JourneyEventBus.getInstance().setOnJourneyBegin(beginJourneyEvent);
            }
            if (index == polyLineList.size() - 1) {
                EndJourneyEvent endJourneyEvent = new EndJourneyEvent();
                endJourneyEvent.setEndJourneyLatLng(new LatLng(polyLineList.get(index).latitude,
                        polyLineList.get(index).longitude));
                JourneyEventBus.getInstance().setOnJourneyEnd(endJourneyEvent);
            }*/
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

            if (index != ployLineLong.size() - 1) {
                mHandler.postDelayed(this, 3000);
            }
        }
    };
    //==========================================================================
    //==========================================================================
    //==========================================================================
    private AppCompatButton btnGo;
    /* private AppCompatEditText editPlace;*/
    private PlaceAutocompleteFragment places;
    private AutocompleteFilter autocompleteFilter;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;
    private IGoogleApiClient googleApiClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


/*    private final View.OnClickListener clickListener = view -> {
        if (view.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null)
            animateCamera(currentLocation);
    };*/

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
        setContentView(R.layout.activity_maps);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        //presence system
        onlineRef=FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUsersRef=FirebaseDatabase.getInstance().getReference().child(driver_location_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUsersRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //findViewById(R.id.currentLocationImageButton).setOnClickListener(clickListener);
        // init view
        locationSwitch = findViewById(R.id.location_switch);
        locationSwitch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    if (isGooglePlayServicesAvailable()) {
                        FirebaseDatabase.getInstance().goOnline();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                        startCurrentLocationUpdates();
                        Toast.makeText(MapsActivity.this, " online", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (fusedLocationProviderClient != null)
                        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                    FirebaseDatabase.getInstance().goOffline();
                    if(currentLocationMarker!=null)
                         currentLocationMarker.remove();
                         googleMap.clear();
                    if(mHandler!=null)
                         mHandler.removeCallbacks(drawPathRunnbale);
                }
            }
        });

        ployLineLong = new ArrayList<>();

        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplet_fragemednt);
        // to restrict search to city
        autocompleteFilter=new AutocompleteFilter.Builder()
                .setCountry("eg")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (locationSwitch.isChecked()) {
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");
                    getDirection();
                } else {
                    Toast.makeText(MapsActivity.this, "Change your status to online", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this, "error " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        googleApiClient = Common.getGoogleApi();
        //geo fire
        mDrivers = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl);
        geoFire = new GeoFire(mDrivers);
        updateFirebaseToken();
    }

    private void updateFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()){
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
                        Toast.makeText(MapsActivity.this, " updated success ", Toast.LENGTH_SHORT).show();
                        reference.child(Common.tokens_tb).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(task.getResult().getToken());
                    }
                    String id=task.getResult().getId();
                    Log.d("user_id",id);
                    String token=task.getResult().getToken();
                    Log.d("user_token",token);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error_in_token",e.getMessage());
            }
        });
    }

    private void getDirection() {
        currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        String requestApi = null;

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccessful = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map));
            if (!isSuccessful) {
                Log.e("Error", "eroro");
            }
        }catch (Resources.NotFoundException ex){
            ex.printStackTrace();
        }

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.setTrafficEnabled(false);
        this.googleMap.setIndoorEnabled(false);
        this.googleMap.setBuildingsEnabled(false);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        Toast.makeText(MapsActivity.this, " online and", Toast.LENGTH_SHORT).show();
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
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission denied by uses", Toast.LENGTH_SHORT).show();
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

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final Long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapaed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapaed / duration);
                float rot = t * i + (1 - t) * startRotation;
                mCurrent.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            //startCurrentLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient = null;
        googleMap = null;
    }
}