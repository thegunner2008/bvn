package tamhoang.bvn.messageCenter.smsServices;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

import tamhoang.bvn.constants.Const;

public class UpdateSMSService extends IntentService {
    public UpdateSMSService() {
        super("UpdateSMSReceiver");
    }

    public void onHandleIntent(Intent intent) {
        markSmsRead(intent.getLongExtra("id", -123));
    }

    public void markSmsRead(long j) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Const.READ, "1");
            ContentResolver contentResolver = getContentResolver();
            contentResolver.update(Uri.parse("content://sms/" + j), contentValues, (String) null, (String[]) null);
        } catch (Exception ignored) {
        }
    }
}
