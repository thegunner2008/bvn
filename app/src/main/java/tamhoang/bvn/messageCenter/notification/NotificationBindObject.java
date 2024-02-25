package tamhoang.bvn.messageCenter.notification;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import tamhoang.bvn.ui.main.MainActivity;
import tamhoang.bvn.ui.trucTiep.FragTructiepXoso;

public class NotificationBindObject {
    private Context mContext;

    public NotificationBindObject(Context context) {
        this.mContext = context;
    }

    @SuppressLint("WrongConstant")
    @JavascriptInterface
    public void showNotification(String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.mContext).setContentText(message).setAutoCancel(true);
        Intent resultIntent = new Intent(this.mContext, MainActivity.class);
        resultIntent.putExtra(FragTructiepXoso.EXTRA_FROM_NOTIFICATION, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        mBuilder.setContentIntent(stackBuilder.getPendingIntent(0, 134217728));
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(-1, mBuilder.build());
    }
}