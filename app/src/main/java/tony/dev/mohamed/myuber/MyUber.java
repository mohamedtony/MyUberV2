package tony.dev.mohamed.myuber;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;
import android.widget.Toast;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyUber extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //for custom font to the whole app
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
        //....

     /*   if (MyHelper.isAppRunning(MyUber.this, "tony.dev.mohamed.myuber")) {
            // App is running
            Toast.makeText(this, " is running ", Toast.LENGTH_SHORT).show();
            Log.d("is_running","is running");
        } else {
            // App is not running
            Toast.makeText(this, "not running ", Toast.LENGTH_SHORT).show();
            Log.d("is_running","not running");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("is_running","not running");

    }*/

}

