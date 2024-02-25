package tamhoang.bvn.util.notifLib.models;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class Action implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() { // from class: tamhoang.ldpro4.util.notifLib.models.Action.1
        @Override // android.os.Parcelable.Creator
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        @Override // android.os.Parcelable.Creator
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
    private final boolean isQuickReply;
    private final PendingIntent p;
    private final String packageName;
    private final ArrayList<RemoteInputParcel> remoteInputs;
    private final String text;
    private final String title;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Action(Parcel in) {
        ArrayList<RemoteInputParcel> arrayList = new ArrayList<>();
        this.remoteInputs = arrayList;
        this.title = in.readString();
        this.text = in.readString();
        this.packageName = in.readString();
        this.p = (PendingIntent) in.readParcelable(PendingIntent.class.getClassLoader());
        this.isQuickReply = in.readByte() != 0;
        in.readTypedList(arrayList, RemoteInputParcel.CREATOR);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.text);
        dest.writeString(this.packageName);
        dest.writeParcelable(this.p, flags);
        dest.writeByte(this.isQuickReply ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.remoteInputs);
    }

    public Action(String title, String text, String packageName, PendingIntent p, RemoteInput remoteInput, boolean isQuickReply) {
        ArrayList<RemoteInputParcel> arrayList = new ArrayList<>();
        this.remoteInputs = arrayList;
        this.title = title;
        this.text = text;
        this.packageName = packageName;
        this.p = p;
        this.isQuickReply = isQuickReply;
        arrayList.add(new RemoteInputParcel(remoteInput));
    }

    public Action(NotificationCompat.Action action, String packageName, boolean isQuickReply) {
        this.remoteInputs = new ArrayList<>();
        this.title = action.title.toString();
        this.text = action.title.toString();
        this.packageName = packageName;
        this.p = action.actionIntent;
        if (action.getRemoteInputs() != null) {
            int length = action.getRemoteInputs().length;
            for (int i = 0; i < length; i++) {
                this.remoteInputs.add(new RemoteInputParcel(action.getRemoteInputs()[i]));
            }
        }
        this.isQuickReply = isQuickReply;
    }

    public void sendReply(Context context, String msg) throws PendingIntent.CanceledException {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        ArrayList arrayList = new ArrayList();
        Iterator<RemoteInputParcel> it = this.remoteInputs.iterator();
        while (it.hasNext()) {
            RemoteInputParcel next = it.next();
            Log.i("", "RemoteInput: " + next.getLabel());
            bundle.putCharSequence(next.getResultKey(), msg);
            RemoteInput.Builder builder = new RemoteInput.Builder(next.getResultKey());
            builder.setLabel(next.getLabel());
            builder.setChoices(next.getChoices());
            builder.setAllowFreeFormInput(next.isAllowFreeFormInput());
            builder.addExtras(next.getExtras());
            arrayList.add(builder.build());
        }
        RemoteInput.addResultsToIntent((RemoteInput[]) arrayList.toArray(new RemoteInput[arrayList.size()]), intent, bundle);
        this.p.send(context, 0, intent);
    }

    public ArrayList<RemoteInputParcel> getRemoteInputs() {
        return this.remoteInputs;
    }

    public boolean isQuickReply() {
        return this.isQuickReply;
    }

    public String getText() {
        return this.text;
    }

    public String getTitle() {
        return this.title;
    }

    public PendingIntent getQuickReplyIntent() {
        if (this.isQuickReply) {
            return this.p;
        }
        return null;
    }

    public String getPackageName() {
        return this.packageName;
    }
}
