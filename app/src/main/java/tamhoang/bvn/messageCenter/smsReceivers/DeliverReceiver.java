package tamhoang.bvn.messageCenter.smsReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import tamhoang.bvn.R;


public class DeliverReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        int resultCode = getResultCode();
        if (resultCode == 0) {
            Toast.makeText(context, R.string.sms_not_delivered, Toast.LENGTH_SHORT).show();
        }
    }
}
