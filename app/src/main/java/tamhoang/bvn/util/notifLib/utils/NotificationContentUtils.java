package tamhoang.bvn.util.notifLib.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import tamhoang.bvn.util.notifLib.models.NotificationIds;

/* loaded from: classes.dex */
public class NotificationContentUtils {
    private static final String TAG = "NotificationContentUtils";

    public static String getTitle(Bundle extras) {
        Log.d(TAG, "Getting title from extras..");
        String string = extras.getString(NotificationCompat.EXTRA_TITLE);
        Log.d("Title Big", "" + extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        return string;
    }

    public static String getTitle(ViewGroup localView) {
        Log.d(TAG, "Getting title..");
        TextView textView = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).TITLE);
        if (textView != null) {
            return textView.getText().toString();
        }
        return null;
    }

    public static String getMessage(Bundle extras) {
        Log.d(TAG, "Getting message from extras..");
        Log.d("Text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_TEXT)));
        Log.d("Big Text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT)));
        Log.d("Title Big", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG)));
        Log.d("Info text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_INFO_TEXT)));
        Log.d("Info text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_INFO_TEXT)));
        Log.d("Subtext", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_SUB_TEXT)));
        Log.d("Summary", "" + extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
        CharSequence charSequence = extras.getCharSequence(NotificationCompat.EXTRA_TEXT);
        if (TextUtils.isEmpty(charSequence)) {
            String string = extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT);
            if (TextUtils.isEmpty(string)) {
                return null;
            }
            return string.toString();
        }
        return charSequence.toString();
    }

    public static String getMessage(ViewGroup localView) {
        TextView textView;
        Log.d(TAG, "Getting message..");
        TextView textView2 = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).BIG_TEXT);
        String charSequence = (textView2 == null || TextUtils.isEmpty(textView2.getText())) ? null : textView2.getText().toString();
        return (!TextUtils.isEmpty(charSequence) || (textView = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).TEXT)) == null) ? charSequence : textView.getText().toString();
    }

    public static String getExtended(Bundle extras, ViewGroup v) {
        Log.d(TAG, "Getting message from extras..");
        CharSequence[] charSequenceArray = extras.getCharSequenceArray(NotificationCompat.EXTRA_TEXT_LINES);
        if (charSequenceArray != null && charSequenceArray.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence charSequence : charSequenceArray) {
                if (!TextUtils.isEmpty(charSequence)) {
                    sb.append(charSequence.toString());
                    sb.append('\n');
                }
            }
            return sb.toString().trim();
        }
        CharSequence charSequence2 = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT);
        return !TextUtils.isEmpty(charSequence2) ? charSequence2.toString() : !VersionUtils.isJellyBeanMR2() ? getExtended(v) : getMessage(extras);
    }

    public static String getExtended(ViewGroup localView) {
        Log.d(TAG, "Getting extended message..");
        NotificationIds notificationIds = NotificationIds.getInstance(localView.getContext());
        TextView textView = (TextView) localView.findViewById(notificationIds.EMAIL_0);
        String str = "";
        if (textView != null && !TextUtils.isEmpty(textView.getText())) {
            str = "" + textView.getText().toString() + '\n';
        }
        TextView textView2 = (TextView) localView.findViewById(notificationIds.EMAIL_1);
        if (textView2 != null && !TextUtils.isEmpty(textView2.getText())) {
            str = str + textView2.getText().toString() + '\n';
        }
        TextView textView3 = (TextView) localView.findViewById(notificationIds.EMAIL_2);
        if (textView3 != null && !TextUtils.isEmpty(textView3.getText())) {
            str = str + textView3.getText().toString() + '\n';
        }
        TextView textView4 = (TextView) localView.findViewById(notificationIds.EMAIL_3);
        if (textView4 != null && !TextUtils.isEmpty(textView4.getText())) {
            str = str + textView4.getText().toString() + '\n';
        }
        TextView textView5 = (TextView) localView.findViewById(notificationIds.EMAIL_4);
        if (textView5 != null && !TextUtils.isEmpty(textView5.getText())) {
            str = str + textView5.getText().toString() + '\n';
        }
        TextView textView6 = (TextView) localView.findViewById(notificationIds.EMAIL_5);
        if (textView6 != null && !TextUtils.isEmpty(textView6.getText())) {
            str = str + textView6.getText().toString() + '\n';
        }
        TextView textView7 = (TextView) localView.findViewById(notificationIds.EMAIL_6);
        if (textView7 != null && !TextUtils.isEmpty(textView7.getText())) {
            str = str + textView7.getText().toString() + '\n';
        }
        if (str.isEmpty()) {
            str = getExpandedText(localView);
        }
        if (str.isEmpty()) {
            str = getMessage(localView);
        }
        return str.trim();
    }

    @SuppressLint({"NewApi"})
    public static ViewGroup getLocalView(Context context, Notification n) {
        RemoteViews view = null;
        if(Build.VERSION.SDK_INT >= 16) {
            view = n.bigContentView;
        }

        if(view == null) {
            view = n.contentView;
        }

        ViewGroup localView = null;

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            localView = (ViewGroup)inflater.inflate(view.getLayoutId(), (ViewGroup)null);
            view.reapply(context, localView);
        } catch (Exception var5) {
            ;
        }

        return localView;
    }

    public static String getExpandedText(ViewGroup localView) {
        NotificationIds notificationIds = NotificationIds.getInstance(localView.getContext());
        String text = "";
        if(localView != null) {
            View v = localView.findViewById(notificationIds.big_notification_content_text);
            View bigTitleView;
            if(v != null && v instanceof TextView) {
                String titleView = ((TextView)v).getText().toString();
                if(!titleView.equals("")) {
                    bigTitleView = localView.findViewById(android.R.id.title);
                    if(v != null && v instanceof TextView) {
                        String inboxTitleView = ((TextView)bigTitleView).getText().toString();
                        if(!inboxTitleView.equals("")) {
                            text = inboxTitleView + " " + titleView;
                        } else {
                            text = titleView;
                        }
                    } else {
                        text = titleView;
                    }
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_10_id);
            CharSequence titleView1;
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("") && !titleView1.equals("")) {
                    text = text + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_9_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_8_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_7_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_6_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_5_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_4_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_3_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_2_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            v = localView.findViewById(notificationIds.inbox_notification_event_1_id);
            if(v != null && v instanceof TextView) {
                titleView1 = ((TextView)v).getText();
                if(!titleView1.equals("")) {
                    text = text + "\n" + titleView1.toString();
                }
            }

            if(text.equals("")) {
                View titleView2 = localView.findViewById(notificationIds.notification_title_id);
                bigTitleView = localView.findViewById(notificationIds.big_notification_title_id);
                View inboxTitleView1 = localView.findViewById(notificationIds.inbox_notification_title_id);
                if(titleView2 != null && titleView2 instanceof TextView) {
                    text = text + ((TextView)titleView2).getText() + " - ";
                } else if(bigTitleView != null && bigTitleView instanceof TextView) {
                    text = text + ((TextView)titleView2).getText();
                } else if(inboxTitleView1 != null && inboxTitleView1 instanceof TextView) {
                    text = text + ((TextView)titleView2).getText();
                }

                v = localView.findViewById(notificationIds.notification_subtext_id);
                if(v != null && v instanceof TextView) {
                    CharSequence s = ((TextView)v).getText();
                    if(!s.equals("")) {
                        text = text + s.toString();
                    }
                }
            }
        }

        return text.trim();
    }
}
