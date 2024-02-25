package tamhoang.bvn.messageCenter.notification;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import tamhoang.bvn.data.Contact;
import tamhoang.bvn.ui.main.MainState;
import tamhoang.bvn.util.notifLib.models.Action;
import tamhoang.bvn.util.notifLib.utils.NotificationUtils;

public class NotificationNewReader extends NotificationReader {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Bundle bundle;
        Notification notification = statusBarNotification.getNotification();
        Notification.WearableExtender wearableExtender = new Notification.WearableExtender(notification);
        if (statusBarNotification.getPackageName().equals(NotificationReader.ZALO) && wearableExtender.getActions().size() == 0
                && (bundle = statusBarNotification.getNotification().extras) != null && bundle.getCharSequence(NotificationCompat.EXTRA_TITLE) != null) {
            this.ID = bundle.getCharSequence(NotificationCompat.EXTRA_TITLE).toString();
            for (int i = 0; i < NotificationCompat.getActionCount(notification); i++) {
                Action quickReplyAction = NotificationUtils.getQuickReplyAction(statusBarNotification.getNotification(), getPackageName());
                if (quickReplyAction == null || quickReplyAction.getTitle() == null) continue;
                if (quickReplyAction.getTitle().contains("Trả lời") || quickReplyAction.getTitle().contains("Reply")) {
                    Contact contact = new Contact();
                    contact.name = "ZL - " + this.ID;
                    contact.app = "ZL";
                    contact.pendingIntent = quickReplyAction.getQuickReplyIntent();
                    contact.remoteInput2 = quickReplyAction.getRemoteInputs().get(0);
                    contact.remoteExtras = statusBarNotification.getNotification().extras;

                    MainState.contactsMap.put("ZL - " + this.ID, contact);
                    if (MainState.mNotifi == null) {
                        MainState.mNotifi = this;
                    }
                }
            }
        }
        super.onNotificationPosted(statusBarNotification);
    }
}
