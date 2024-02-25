package tamhoang.bvn.util.notifLib.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import tamhoang.bvn.util.notifLib.models.Action;
import tamhoang.bvn.util.notifLib.models.NotificationIds;

/* loaded from: classes.dex */
public class NotificationUtils {
    private static final String[] REPLY_KEYWORDS = {"reply", "Trả lời", "android.intent.extra.text"};
    private static final CharSequence REPLY_KEYWORD = "reply";
    private static final CharSequence REPLY_KEYWORD2 = "Trả lời";
    private static final CharSequence INPUT_KEYWORD = "input";

    public static boolean isAPriorityMode(int interruptionFilter) {
        return (interruptionFilter == 3 || interruptionFilter == 0) ? false : true;
    }

    public static boolean isRecent(StatusBarNotification sbn, long recentTimeframeInSecs) {
        return sbn.getNotification().when > 0 && System.currentTimeMillis() - sbn.getNotification().when <= TimeUnit.SECONDS.toMillis(recentTimeframeInSecs);
    }

    public static boolean notificationMatchesFilter(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap) {
        NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
        return rankingMap.getRanking(sbn.getKey(), ranking) && ranking.matchesInterruptionFilter();
    }

    public static String getMessage(Bundle extras) {
        Log.d("NOTIFICATIONUTILS", "Getting message from extras..");
        Log.d("Text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_TEXT)));
        Log.d("Big Text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT)));
        Log.d("Title Big", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG)));
        Log.d("Info text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_INFO_TEXT)));
        Log.d("Info text", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_INFO_TEXT)));
        Log.d("Subtext", "" + ((Object) extras.getCharSequence(NotificationCompat.EXTRA_SUB_TEXT)));
        Log.d("Summary", "" + extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
        CharSequence charSequence = extras.getCharSequence(NotificationCompat.EXTRA_TEXT);
        if (!TextUtils.isEmpty(charSequence)) {
            return charSequence.toString();
        }
        String string = extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT);
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        return string.toString();
    }

    public static String getExtended(Bundle extras, ViewGroup v) {
        Log.d("NOTIFICATIONUTILS", "Getting message from extras..");
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
        if (!TextUtils.isEmpty(charSequence2)) {
            return charSequence2.toString();
        }
        if (!VersionUtils.isJellyBeanMR2()) {
            return getExtended(v);
        }
        return getMessage(extras);
    }

    public static ViewGroup getMessageView(Context context, Notification n) {
        Log.d("NOTIFICATIONUTILS", "Getting message view..");
        RemoteViews remoteViews = Build.VERSION.SDK_INT >= 16 ? n.bigContentView : null;
        if (remoteViews == null) {
            remoteViews = n.contentView;
        }
        if (remoteViews == null) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(remoteViews.getLayoutId(), (ViewGroup) null);
        remoteViews.reapply(context.getApplicationContext(), viewGroup);
        return viewGroup;
    }

    public static String getTitle(ViewGroup localView) {
        Log.d("NOTIFICATIONUTILS", "Getting title..");
        TextView textView = (TextView) localView.findViewById(NotificationIds.getInstance(localView.getContext()).TITLE);
        if (textView != null) {
            return textView.getText().toString();
        }
        return null;
    }

    public static String getMessage(ViewGroup localView) {
        TextView textView;
        Log.d("NOTIFICATIONUTILS", "Getting message..");
        Context context = localView.getContext();
        TextView textView2 = (TextView) localView.findViewById(NotificationIds.getInstance(context).BIG_TEXT);
        String charSequence = (textView2 == null || TextUtils.isEmpty(textView2.getText())) ? null : textView2.getText().toString();
        return (!TextUtils.isEmpty(charSequence) || (textView = (TextView) localView.findViewById(NotificationIds.getInstance(context).TEXT)) == null) ? charSequence : textView.getText().toString();
    }

    public static String getExtended(ViewGroup localView) {
        Log.d("NOTIFICATIONUTILS", "Getting extended message..");
        Context context = localView.getContext();
        TextView textView = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_0);
        String str = "";
        if (textView != null && !TextUtils.isEmpty(textView.getText())) {
            str = "" + textView.getText().toString() + '\n';
        }
        TextView textView2 = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_1);
        if (textView2 != null && !TextUtils.isEmpty(textView2.getText())) {
            str = str + textView2.getText().toString() + '\n';
        }
        TextView textView3 = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_2);
        if (textView3 != null && !TextUtils.isEmpty(textView3.getText())) {
            str = str + textView3.getText().toString() + '\n';
        }
        TextView textView4 = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_3);
        if (textView4 != null && !TextUtils.isEmpty(textView4.getText())) {
            str = str + textView4.getText().toString() + '\n';
        }
        TextView textView5 = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_4);
        if (textView5 != null && !TextUtils.isEmpty(textView5.getText())) {
            str = str + textView5.getText().toString() + '\n';
        }
        TextView textView6 = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_5);
        if (textView6 != null && !TextUtils.isEmpty(textView6.getText())) {
            str = str + textView6.getText().toString() + '\n';
        }
        TextView textView7 = (TextView) localView.findViewById(NotificationIds.getInstance(context).EMAIL_6);
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

    public static String getTitle(Bundle extras) {
        Log.d("NOTIFICATIONUTILS", "Getting title from extras..");
        String string = extras.getString(NotificationCompat.EXTRA_TITLE);
        Log.d("Title Big", "" + extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        return string;
    }

    public static ViewGroup getView(Context context, RemoteViews view)
    {
        ViewGroup localView = null;
        try
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            localView = (ViewGroup) inflater.inflate(view.getLayoutId(), null);
            view.reapply(context, localView);
        }
        catch (Exception exp)
        {
        }
        return localView;
    }

    @SuppressLint("NewApi")
    public static ViewGroup getLocalView(Context context, Notification n)
    {
        RemoteViews view = null;
        view = n.bigContentView;

        if (view == null)
        {
            view = n.contentView;
        }
        ViewGroup localView = null;
        try
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            localView = (ViewGroup) inflater.inflate(view.getLayoutId(), null);
            view.reapply(context, localView);
        } catch (Exception exp) { }
        return localView;
    }

    public static ArrayList<Action> getActions(Notification n, String packageName, ArrayList<Action> actions) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(n);
        if (wearableExtender.getActions().size() > 0) {
            for (NotificationCompat.Action action : wearableExtender.getActions()) {
                boolean z = false;
                if (action.title.toString().toLowerCase().contains(REPLY_KEYWORD) || action.title.toString().toLowerCase().contains(REPLY_KEYWORD2)) {
                    z = true;
                }
                actions.add(new Action(action, packageName, z));
            }
        }
        return actions;
    }

    public static Action getQuickReplyAction(Notification n, String packageName) {
        NotificationCompat.Action quickReplyAction = Build.VERSION.SDK_INT >= 24 ? getQuickReplyAction(n) : null;
        if (quickReplyAction == null) {
            quickReplyAction = getWearReplyAction(n);
        }
        if (quickReplyAction == null) {
            return null;
        }
        return new Action(quickReplyAction, packageName, true);
    }

    private static NotificationCompat.Action getQuickReplyAction(Notification n) {
        for (int i = 0; i < NotificationCompat.getActionCount(n); i++) {
            NotificationCompat.Action action = NotificationCompat.getAction(n, i);
            if (action.getRemoteInputs() != null) {
                for (int i2 = 0; i2 < action.getRemoteInputs().length; i2++) {
                    if (isKnownReplyKey(action.getRemoteInputs()[i2].getResultKey())) {
                        return action;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private static NotificationCompat.Action getWearReplyAction(Notification n) {
        Iterator<NotificationCompat.Action> it = new NotificationCompat.WearableExtender(n).getActions().iterator();
        while (it.hasNext()) {
            NotificationCompat.Action next = it.next();
            if (next.getRemoteInputs() != null) {
                for (int i = 0; i < next.getRemoteInputs().length; i++) {
                    RemoteInput remoteInput = next.getRemoteInputs()[i];
                    if (isKnownReplyKey(remoteInput.getResultKey()) || remoteInput.getResultKey().toLowerCase().contains(INPUT_KEYWORD)) {
                        return next;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private static boolean isKnownReplyKey(String resultKey) {
        if (TextUtils.isEmpty(resultKey)) {
            return false;
        }
        String lowerCase = resultKey.toLowerCase();
        for (String str : REPLY_KEYWORDS) {
            if (lowerCase.contains(str)) {
                return true;
            }
        }
        return false;
    }

    //OLD METHOD
    public static String getExpandedText(ViewGroup localView)
    {
        String text = "";
        if (localView != null)
        {
            Context context = localView.getContext();
            View v;
            // try to get big text
            v = localView.findViewById(NotificationIds.getInstance(context).big_notification_content_text);
            if (v != null && v instanceof TextView)
            {
                String s = ((TextView)v).getText().toString();
                if (!s.equals(""))
                {
                    // add title string if available
                    View titleView = localView.findViewById(android.R.id.title);
                    if (v != null && v instanceof TextView)
                    {
                        String title = ((TextView)titleView).getText().toString();
                        if (!title.equals(""))
                            text = title + " " + s;
                        else
                            text = s;
                    }
                    else
                        text = s;
                }
            }

            // try to extract details lines
            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_10_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    if (!s.equals(""))
                        text += s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_9_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_8_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_7_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_6_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_5_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_4_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_3_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_2_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            v = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_event_1_id);
            if (v != null && v instanceof TextView)
            {
                CharSequence s = ((TextView)v).getText();
                if (!s.equals(""))
                    text += "\n" + s.toString();
            }

            if (text.equals("")) //Last resort for Kik
            {
                // get title string if available
                View titleView = localView.findViewById(NotificationIds.getInstance(context).notification_title_id );
                View bigTitleView = localView.findViewById(NotificationIds.getInstance(context).big_notification_title_id );
                View inboxTitleView = localView.findViewById(NotificationIds.getInstance(context).inbox_notification_title_id );
                if (titleView  != null && titleView  instanceof TextView)
                {
                    text += ((TextView)titleView).getText() + " - ";
                } else if (bigTitleView != null && bigTitleView instanceof TextView)
                {
                    text += ((TextView)titleView).getText();
                } else if  (inboxTitleView != null && inboxTitleView instanceof TextView)
                {
                    text += ((TextView)titleView).getText();
                }

                v = localView.findViewById(NotificationIds.getInstance(context).notification_subtext_id);
                if (v != null && v instanceof TextView)
                {
                    CharSequence s = ((TextView)v).getText();
                    if (!s.equals(""))
                    {
                        text += s.toString();
                    }
                }
            }

        }
        return text.trim();
    }
}
