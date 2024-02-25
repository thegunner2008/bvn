package tamhoang.bvn.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ZBroadcast extends Service {
    String viewData;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        return 1;
    }

    public void onCreate() {
        super.onCreate();
    }
}
