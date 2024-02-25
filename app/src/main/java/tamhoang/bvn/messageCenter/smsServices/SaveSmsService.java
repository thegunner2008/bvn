package tamhoang.bvn.messageCenter.smsServices;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import tamhoang.bvn.constants.SmsContract;

public class SaveSmsService extends IntentService {
    public SaveSmsService() {
        super("SaveService");
    }

    /* access modifiers changed from: protected */
    public void onHandleIntent(Intent intent) {
        String stringExtra = intent.getStringExtra("sender_no");
        String stringExtra2 = intent.getStringExtra("message");
        long longExtra = intent.getLongExtra("date", 0);
        ContentValues contentValues = new ContentValues();
        contentValues.put("address", stringExtra);
        contentValues.put("body", stringExtra2);
        contentValues.put("date_sent", longExtra);
        getContentResolver().insert(SmsContract.ALL_SMS_URI, contentValues);
        sendBroadcast(new Intent("android.intent.action.MAIN").putExtra("new_sms", true));
    }
}
