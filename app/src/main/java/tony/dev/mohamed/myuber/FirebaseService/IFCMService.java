package tony.dev.mohamed.myuber.FirebaseService;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.models.DataMessage;
import tony.dev.mohamed.myuber.models.FCMResponse;

public interface IFCMService {
    //service for sending message using retrofit
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAVikAqZE:APA91bHumu_GToW1T0LRb5eg3GvxzFzu4SQ8kKk3FoDLUw3o7rlr-iPGzFnw9id8og0_ZrPAptEK-mk6B2w16_c34MLHFIpT7ych5nH96lTjLi0l2k_dsZTQF6c_XpPeWC3ju4rJFTaf"
    })
    @POST("fcm/send")
    Call<FCMResponse>sendMessage(@Body DataMessage body);
}
