package libusbcamera.jadeh.uvccamera;

import android.annotation.SuppressLint;
import android.app.Application;

/**
 * @author zhanghongqiang
 * @date 2019-06-17 15:42
 */
public class App extends Application {

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
