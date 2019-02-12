package tony.dev.mohamed.myuber.Retrofit;

import android.location.Location;

import tony.dev.mohamed.myuber.FirebaseService.IFCMService;
import tony.dev.mohamed.myuber.models.UberDriver;

public class Common {
    public static final String driver_location_tbl="Drivers";
    public static final String user_driver_tbl="DriversInformation";
    public static final String user_rider_tbl="RidersInformation";
    public static final String pickup_request_tb="PickupRequest";
    public static final String tokens_tb="Tokens";
    public static Location currentLocation=null;
    public static UberDriver uberDriver=new UberDriver();


    public static String userSignInEmail;
    public static String userSignInPass;
    public static double base_fare=2.55;
    public static double time_rate=0.35;
    public static double distance_rate=1.75;
    public static double getPrice(double km,int min){
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }


    public static final String baseUrl="https://maps.googleapis.com";
    public static final String fcmUrl="https://fcm.googleapis.com";

    public static IGoogleApiClient getGoogleApi(){
        return RetrofitClient.getClient(baseUrl).create(IGoogleApiClient.class);
    }
    public static IFCMService getFcmService(){
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }
}
