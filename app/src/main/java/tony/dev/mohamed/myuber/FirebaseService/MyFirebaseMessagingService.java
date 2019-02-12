package tony.dev.mohamed.myuber.FirebaseService;

import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;
import java.util.logging.Handler;

import tony.dev.mohamed.myuber.CustomerCall;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.models.Token;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTkenToService(s);
    }

    private void updateTkenToService(String tokenStr) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Token token=new Token(tokenStr);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            reference.child(Common.tokens_tb).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //when the ridder send to the driver this service will work
        if (remoteMessage.getData() != null) {
            Map<String,String> data=remoteMessage.getData();
            final String customer=data.get("customer");
            final String lat=data.get("lat");
            final String lang=data.get("lang");
          //  final String message=data.get("message");
           // LatLng location = new Gson().fromJson(message, LatLng.class);
            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                   Log.e("service_my","custorerId: "+customer+" lat : "+lat+" lng : "+lang);
                }
            });

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat",lat);
            intent.putExtra("lng", lang);
            intent.putExtra("customerId",customer);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
