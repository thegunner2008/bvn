package tamhoang.bvn.util.notifLib.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.fragment.app.Fragment;

/* loaded from: classes.dex */
public class NotificationListenerUtils {
    private static final String LISTENER_SERVICE_CONNECTED = "LISTENER_SERVICE_CONNECTED";
    public static final int REQ_NOTIFICATION_LISTENER = 123;
    private static final String TAG = "NotificationListener";

    public static void setListenerConnected(Context context, boolean listenerConnected) {
        SharedPreferences.Editor edit = context.getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS).edit();
        edit.putBoolean(LISTENER_SERVICE_CONNECTED, listenerConnected);
        edit.commit();
    }

    public static boolean isListenerConnected(Context context) {
        return context.getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS).getBoolean(LISTENER_SERVICE_CONNECTED, false);
    }

    public static boolean isListenerEnabled(Context context, Class notificationListenerCls) {
        ComponentName componentName = new ComponentName(context, notificationListenerCls);
        String string = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return string != null && string.contains(componentName.flattenToString());
    }

    public static void launchNotificationAccessSettings(Activity activity) {
        activity.startActivityForResult(new Intent(VersionUtils.isJellyBeanMR2() ? "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS" : "android.settings.ACCESSIBILITY_SETTINGS"), 123);
    }

    public static void launchNotificationAccessSettings(Fragment fragment) {
        fragment.startActivityForResult(new Intent(VersionUtils.isJellyBeanMR2() ? "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS" : "android.settings.ACCESSIBILITY_SETTINGS"), 123);
    }
}
