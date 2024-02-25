package tamhoang.bvn.util.notifLib.models;

import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.ScheduledFuture;
import tamhoang.bvn.util.notifLib.utils.VersionUtils;

/* loaded from: classes.dex */
public class PendingNotification {
    private String key;
    private StatusBarNotification sbn;
    private ScheduledFuture<?> scheduledFuture;

    public PendingNotification(StatusBarNotification sbn) {
        this.sbn = sbn;
        this.key = VersionUtils.isLollipop() ? sbn.getKey() : null;
    }

    public void setDismissKey(String key) {
        this.key = key;
    }

    public String getDismissKey() {
        return this.key;
    }

    public StatusBarNotification getSbn() {
        return this.sbn;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return this.scheduledFuture;
    }

    public boolean equals(Object o) {
        if (VersionUtils.isJellyBean()) {
            String group = NotificationCompat.getGroup(((PendingNotification) o).getSbn().getNotification());
            String group2 = NotificationCompat.getGroup(this.sbn.getNotification());
            if (group == null || group2 == null) {
                return false;
            }
            return group.equals(group2);
        }
        return ((PendingNotification) o).getSbn().getPackageName().equals(this.sbn.getPackageName());
    }
}
