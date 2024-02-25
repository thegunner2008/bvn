package tamhoang.bvn.util.notifLib.utils;

import android.app.Notification;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/* loaded from: classes.dex */
public class NotificationDebugUtils {
    public static void printNotification(StatusBarNotification sbn) {
        String simpleName = NotificationDebugUtils.class.getSimpleName();
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle(sbn.getNotification()));
        sb.append(", ");
        sb.append(getMessageContent(sbn.getNotification()));
        sb.append(", Sbn id: ");
        sb.append(sbn.getId());
        sb.append(", Sbn tag: ");
        sb.append(sbn.getTag());
        sb.append(", Sbn key: ");
        sb.append(Build.VERSION.SDK_INT >= 20 ? sbn.getKey() : "Unknown");
        sb.append(", Post time: ");
        sb.append(sbn.getPostTime());
        sb.append(", When time: ");
        sb.append(sbn.getNotification().when);
        sb.append(", ");
        Log.d(simpleName, sb.toString());
    }

    public static String getTitle(Notification n) {
        return NotificationCompat.getExtras(n).getString(NotificationCompat.EXTRA_TITLE);
    }

    public static String getMessageContent(Notification n) {
        return NotificationCompat.getExtras(n).getString(NotificationCompat.EXTRA_TEXT);
    }
}
