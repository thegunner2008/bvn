package tamhoang.bvn.util.notifLib.models;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import okhttp3.internal.cache.DiskLruCache;

/* loaded from: classes.dex */
public class NotificationIds {
    private static NotificationIds singleton;
    public int BIG_PIC;
    public int BIG_TEXT;
    public int EMAIL_0;
    public int EMAIL_1;
    public int EMAIL_2;
    public int EMAIL_3;
    public int EMAIL_4;
    public int EMAIL_5;
    public int EMAIL_6;
    public int ICON;
    public int INBOX_MORE;
    public int TEXT;
    public int TITLE;
    public int notification_title_id = 0;
    public int big_notification_summary_id = 0;
    public int big_notification_content_title = 0;
    public int big_notification_content_text = 0;
    public int notification_image_id = 0;
    public int inbox_notification_title_id = 0;
    public int big_notification_title_id = 0;
    public int notification_subtext_id = 0;
    public int inbox_notification_event_1_id = 0;
    public int inbox_notification_event_2_id = 0;
    public int inbox_notification_event_3_id = 0;
    public int inbox_notification_event_4_id = 0;
    public int inbox_notification_event_5_id = 0;
    public int inbox_notification_event_6_id = 0;
    public int inbox_notification_event_7_id = 0;
    public int inbox_notification_event_8_id = 0;
    public int inbox_notification_event_9_id = 0;
    public int inbox_notification_event_10_id = 0;

    public static NotificationIds getInstance(Context context) {
        if (singleton == null) {
            singleton = new NotificationIds(context);
        }
        return singleton;
    }

    /* JADX WARN: Type inference failed for: r1v2, types: [tamhoang.ldpro4.util.notifLib.models.NotificationIds$1] */
    public NotificationIds(final Context context) {
        Resources resources = context.getResources();
        this.ICON = resources.getIdentifier("android:id/icon", null, null);
        this.TITLE = resources.getIdentifier("android:id/title", null, null);
        this.BIG_TEXT = resources.getIdentifier("android:id/big_text", null, null);
        this.TEXT = resources.getIdentifier("android:id/text", null, null);
        this.BIG_PIC = resources.getIdentifier("android:id/big_picture", null, null);
        this.EMAIL_0 = resources.getIdentifier("android:id/inbox_text0", null, null);
        this.EMAIL_1 = resources.getIdentifier("android:id/inbox_text1", null, null);
        this.EMAIL_2 = resources.getIdentifier("android:id/inbox_text2", null, null);
        this.EMAIL_3 = resources.getIdentifier("android:id/inbox_text3", null, null);
        this.EMAIL_4 = resources.getIdentifier("android:id/inbox_text4", null, null);
        this.EMAIL_5 = resources.getIdentifier("android:id/inbox_text5", null, null);
        this.EMAIL_6 = resources.getIdentifier("android:id/inbox_text6", null, null);
        this.INBOX_MORE = resources.getIdentifier("android:id/inbox_more", null, null);
        new AsyncTask() { // from class: tamhoang.ldpro4.util.notifLib.models.NotificationIds.1
            @Override // android.os.AsyncTask
            protected Object doInBackground(Object[] params) {
                return null;
            }

            @Override // android.os.AsyncTask
            protected void onPostExecute(Object o) {
                NotificationIds.this.detectNotificationIds(context);
            }
        }.execute(new Object[0]);
    }

    private void recursiveDetectNotificationsIds(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            View childAt = v.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                recursiveDetectNotificationsIds((ViewGroup) childAt);
            } else if (childAt instanceof TextView) {
                String charSequence = ((TextView) childAt).getText().toString();
                int id = childAt.getId();
                if (charSequence.equals(DiskLruCache.VERSION_1)) {
                    this.notification_title_id = id;
                } else if (charSequence.equals("4")) {
                    this.notification_subtext_id = id;
                } else if (charSequence.equals("5")) {
                    this.big_notification_summary_id = id;
                } else if (charSequence.equals("6")) {
                    this.big_notification_content_title = id;
                } else if (charSequence.equals("7")) {
                    this.big_notification_content_text = id;
                } else if (charSequence.equals("8")) {
                    this.big_notification_title_id = id;
                } else if (charSequence.equals("9")) {
                    this.inbox_notification_title_id = id;
                } else if (charSequence.equals("10")) {
                    this.inbox_notification_event_1_id = id;
                } else if (charSequence.equals("11")) {
                    this.inbox_notification_event_2_id = id;
                } else if (charSequence.equals("12")) {
                    this.inbox_notification_event_3_id = id;
                } else if (charSequence.equals("13")) {
                    this.inbox_notification_event_4_id = id;
                } else if (charSequence.equals("14")) {
                    this.inbox_notification_event_5_id = id;
                } else if (charSequence.equals("15")) {
                    this.inbox_notification_event_6_id = id;
                } else if (charSequence.equals("16")) {
                    this.inbox_notification_event_7_id = id;
                } else if (charSequence.equals("17")) {
                    this.inbox_notification_event_8_id = id;
                } else if (charSequence.equals("18")) {
                    this.inbox_notification_event_9_id = id;
                } else if (charSequence.equals("19")) {
                    this.inbox_notification_event_10_id = id;
                }
            } else if ((childAt instanceof ImageView) && ((ImageView) childAt).getDrawable() != null) {
                this.notification_image_id = childAt.getId();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void detectNotificationIds(Context context) {
        NotificationCompat.Builder subText = new NotificationCompat.Builder(context).setContentTitle(DiskLruCache.VERSION_1).setContentText("2").setContentInfo("3").setSubText("4");
        Notification build = subText.build();
        ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(build.contentView.getLayoutId(), (ViewGroup) null);
        build.contentView.reapply(context, viewGroup);
        recursiveDetectNotificationsIds(viewGroup);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setSummaryText("5");
        bigTextStyle.setBigContentTitle("6");
        bigTextStyle.bigText("7");
        subText.setContentTitle("8");
        subText.setStyle(bigTextStyle);
        detectExpandedNotificationsIds(subText.build(), context);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] strArr = {"10", "11", "12", "13", "14", "15", "16", "17", "18", "19"};
        inboxStyle.setBigContentTitle("6");
        subText.setContentTitle("9");
        inboxStyle.setSummaryText("5");
        for (int i = 0; i < 10; i++) {
            inboxStyle.addLine(strArr[i]);
        }
        subText.setStyle(inboxStyle);
        detectExpandedNotificationsIds(subText.build(), context);
    }

    private void detectExpandedNotificationsIds(Notification n, Context context) {
        ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(n.bigContentView.getLayoutId(), (ViewGroup) null);
        n.bigContentView.reapply(context, viewGroup);
        recursiveDetectNotificationsIds(viewGroup);
    }
}
