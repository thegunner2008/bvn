package tamhoang.bvn.util.notifLib.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.app.RemoteInput;

/* loaded from: classes.dex */
public class RemoteInputParcel implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() { // from class: tamhoang.ldpro4.util.notifLib.models.RemoteInputParcel.1
        @Override // android.os.Parcelable.Creator
        public RemoteInputParcel createFromParcel(Parcel in) {
            return new RemoteInputParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteInputParcel[] newArray(int size) {
            return new RemoteInputParcel[size];
        }
    };
    private boolean allowFreeFormInput;
    private String[] choices;
    private Bundle extras;
    private String label;
    private String resultKey;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public RemoteInputParcel(RemoteInput input) {
        this.choices = new String[0];
        this.label = input.getLabel().toString();
        this.resultKey = input.getResultKey();
        charSequenceToStringArray(input.getChoices());
        this.allowFreeFormInput = input.getAllowFreeFormInput();
        this.extras = input.getExtras();
    }

    public RemoteInputParcel(Parcel in) {
        this.choices = new String[0];
        this.label = in.readString();
        this.resultKey = in.readString();
        this.choices = in.createStringArray();
        this.allowFreeFormInput = in.readByte() != 0;
        this.extras = (Bundle) in.readParcelable(Bundle.class.getClassLoader());
    }

    public void charSequenceToStringArray(CharSequence[] charSequence) {
        if (charSequence != null) {
            int length = charSequence.length;
            this.choices = new String[charSequence.length];
            for (int i = 0; i < length; i++) {
                this.choices[i] = charSequence[i].toString();
            }
        }
    }

    public String getResultKey() {
        return this.resultKey;
    }

    public String getLabel() {
        return this.label;
    }

    public CharSequence[] getChoices() {
        return this.choices;
    }

    public boolean isAllowFreeFormInput() {
        return this.allowFreeFormInput;
    }

    public Bundle getExtras() {
        return this.extras;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.label);
        dest.writeString(this.resultKey);
        dest.writeStringArray(this.choices);
        dest.writeByte(this.allowFreeFormInput ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.extras, flags);
    }
}
