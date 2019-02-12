package tony.dev.mohamed.myuber;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

import tony.dev.mohamed.myuber.Retrofit.Common;

public class TripDetail extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView txtDate, txtPrice, txtBaseFare, txtTimeValue, txtDistanceValue, txtPriceValue, txtFromFrom, txtToTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //init views
        txtDate = findViewById(R.id.txtDate);
        txtPrice = findViewById(R.id.txtPrice);
        txtBaseFare = findViewById(R.id.txtBaseFare);
        txtTimeValue = findViewById(R.id.txtTimeValue);
        txtDistanceValue = findViewById(R.id.txtDistanceValue);
        txtPriceValue = findViewById(R.id.txtPriceValue);
        txtFromFrom = findViewById(R.id.txtFromFrom);
        txtToTo = findViewById(R.id.txtToTo);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the uberDriver will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the uberDriver has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (getIntent() != null) {
            settingInformation();
        }
    }

    private void settingInformation() {
        //setting informatiom
        Calendar calendar = Calendar.getInstance();
        String date = String.format("%s, %d/%d", convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1);
        txtDate.setText(date);
        txtPrice.setText(getIntent().getStringExtra("total"));
        txtPriceValue.setText(getIntent().getStringExtra("total"));
        txtBaseFare.setText(String.format("$ %.2f",Common.base_fare));
        txtTimeValue.setText(String.format("%s min",getIntent().getStringExtra("time")));
        txtDistanceValue.setText(String.format("%s km",getIntent().getStringExtra("distance")));
        txtFromFrom.setText(getIntent().getStringExtra("start_address"));
        txtToTo.setText(getIntent().getStringExtra("endAddress"));
        Log.d("toal_ calculatio",String.valueOf(getIntent().getDoubleExtra("total",0.0)));

        //add Marker
        String []location_end=getIntent().getStringExtra("location_end").split(",");
        LatLng dropOff=new LatLng(Double.parseDouble(location_end[0]),Double.parseDouble(location_end[1]));
        mMap.addMarker(new MarkerOptions()
                .position(dropOff)
                .title("DropOff HERE")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        );

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOff,12.0f));

    }

    private String convertToDayOfWeek(int day) {
        switch (day) {
            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FEBRUARY";
            case Calendar.SATURDAY:
                return "SATURDAY";
            default:
                return "UNK";
        }
    }
}
