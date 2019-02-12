package tony.dev.mohamed.myuber;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuber.FirebaseService.IFCMService;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.Retrofit.IGoogleApiClient;
import tony.dev.mohamed.myuber.models.DataMessage;
import tony.dev.mohamed.myuber.models.FCMResponse;
import tony.dev.mohamed.myuber.models.Token;

import static java.lang.String.valueOf;
import static tony.dev.mohamed.myuber.Retrofit.Common.currentLocation;

public class CustomerCall extends AppCompatActivity {
    private TextView textTime, textDistance, textAddress, textConter;
    private MediaPlayer mediaPlayer;
    private IGoogleApiClient googleApiClient;
    private IFCMService ifcmService;
    private Button accept_btn, decline_btn;
    private String customerId;
    private String lat;
    private String lng;
    private CountDownTimer countDownTimer;
    public native String getGoogleDirectionApi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        //init views
        textTime = findViewById(R.id.textTime);
        textConter = findViewById(R.id.text_count);
        textDistance = findViewById(R.id.textDistance);
        textAddress = findViewById(R.id.textAddress);
        googleApiClient = Common.getGoogleApi();
        accept_btn = findViewById(R.id.accept_btn);
        decline_btn = findViewById(R.id.decline_btn);
        ifcmService = Common.getFcmService();

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (countDownTimer != null)
                    countDownTimer.cancel();
                Intent intent = new Intent(CustomerCall.this, DriverTracking.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("customerId", customerId);
                startActivity(intent);
            }
        });
        decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(customerId)) {
                    cancelBooking(customerId);
                }
            }
        });

        //init mediaplayer
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        //get intent value from firebase service
        if (getIntent() != null) {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customerId");

            getDirection(lat, lng);

            startTimer();
        }
    }

    private void startTimer() {


        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable run = new Runnable() {

            @Override
            public void run() {
                int totalTimeCountInMilliseconds = 30000;
                countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 1000) {

                    @Override
                    public void onTick(long leftTimeInMilliseconds) {
                        //tv_resend.setEnabled(false);
                        long seconds = leftTimeInMilliseconds / 1000;

                            /*int sec = (int) seconds;
                            int minints = sec / 60;
                            int socnd = sec % 60;
                            String n = String.format(Locale.US, "%02d : %02d ", minints, socnd);*/
                        textConter.setText(valueOf(seconds));
                    }

                    @Override
                    public void onFinish() {
                        // this function will be called when the timecount is finished
                        //tv_resend.setEnabled(true);
                        if (!TextUtils.isEmpty(customerId)) {
                            cancelBooking(customerId);
                        } else {
                            Toast.makeText(CustomerCall.this, " customer id is null", Toast.LENGTH_SHORT).show();
                        }

                    }

                }.start();

            }
        };
        mainHandler.post(run);


    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);
      /*  Toast.makeText(this, " customer id : "+customerId, Toast.LENGTH_SHORT).show();
        Log.d("customer_id",customerId);
        Token token=new Token(customerId);
        Notification notification=new Notification("Cancel","the driver has cancelled your request");
        Sender sender=new Sender(notification,token.getToken());*/
        HashMap<String, String> content = new HashMap<>();
        content.put("title", "Cancel");
        content.put("message", "the driver has cancelled your request");
        DataMessage dataMessage = new DataMessage(token.getToken(), content);

        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success == 1) {
                    Toast.makeText(CustomerCall.this, "Your Request cancelled !", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.d("eroro", t.getMessage());
            }
        });


    }

    private void getDirection(String lat, String lng) {
        String requestApi = null;

//        String apiKey = Arrays.toString(Base64.decode(getGoogleDirectionApi(), Base64.DEFAULT));

        //request to direction of the drivers and show address - calculate time and distance
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getString(R.string.google_direction_api);
            googleApiClient.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject jsonObject1 = routes.getJSONObject(0);
                        JSONArray legs = jsonObject1.getJSONArray("legs");
                        JSONObject jsonObject2 = legs.getJSONObject(0);
                        //get distance
                        JSONObject distance = jsonObject2.getJSONObject("distance");
                        textDistance.setText(distance.getString("text"));
                        //get duration
                        JSONObject duration = jsonObject2.getJSONObject("duration");
                        textTime.setText(duration.getString("text"));
                        //get address
                        textAddress.setText(jsonObject2.getString("end_address"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CustomerCall.this, " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("eroro", t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("error_incustomer", e.getMessage());
            Toast.makeText(CustomerCall.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }


    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }
}

