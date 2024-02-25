package tamhoang.bvn.messageCenter.smsReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SentReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        int resultCode = getResultCode();
        if (resultCode == -1) {
            return;
        }
        if (resultCode == 1) {
            Toast.makeText(context, "Sai số điện thoại", Toast.LENGTH_SHORT).show();
        } else if (resultCode == 2) {
            Toast.makeText(context, "Không có mạng!", Toast.LENGTH_SHORT).show();
        } else if (resultCode == 3) {
            Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
        } else if (resultCode == 4) {
            Toast.makeText(context, "Không có dịch vụ", Toast.LENGTH_SHORT).show();
        }
    }
}
