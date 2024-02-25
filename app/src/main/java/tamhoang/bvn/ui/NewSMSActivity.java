package tamhoang.bvn.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import tamhoang.bvn.R;
import tamhoang.bvn.messageCenter.smsReceivers.DeliverReceiver;
import tamhoang.bvn.messageCenter.smsReceivers.SentReceiver;

public class NewSMSActivity extends AppCompatActivity implements View.OnClickListener {
    BroadcastReceiver deliveryBroadcastReciever = new DeliverReceiver();
    private String message;
    private String phoneNo;
    BroadcastReceiver sendBroadcastReceiver = new SentReceiver();
    private EditText txtMessage;
    private EditText txtphoneNo;

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.SupportActivity, android.support.v4.app.FragmentActivity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_sms_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
    }

    private void init() {
        this.txtphoneNo = (EditText) findViewById(R.id.editText);
        this.txtMessage = (EditText) findViewById(R.id.editText2);
        ((ImageButton) findViewById(R.id.contact)).setOnClickListener(new View.OnClickListener() {
            /* class tamhoang.ldpro4.Activity.NewSMSActivity.AnonymousClass1 */

            public void onClick(View v) {
                NewSMSActivity.this.startActivityForResult(new Intent("android.intent.action.PICK", ContactsContract.CommonDataKinds.Phone.CONTENT_URI), 85);
            }
        });
        ((Button) findViewById(R.id.btnSendSMS)).setOnClickListener(this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnSendSMS) {
            this.phoneNo = this.txtphoneNo.getText().toString();
            this.message = this.txtMessage.getText().toString();
            String str = this.phoneNo;
            if (str == null || str.trim().length() <= 0) {
                this.txtphoneNo.setError(getString(R.string.please_write_number));
                return;
            }
            String str2 = this.message;
            if (str2 == null || str2.trim().length() <= 0) {
                this.txtMessage.setError(getString(R.string.please_write_message));
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(this.sendBroadcastReceiver);
            unregisterReceiver(this.deliveryBroadcastReciever);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pickContact(View v) {
        startActivityForResult(new Intent("android.intent.action.PICK", ContactsContract.CommonDataKinds.Phone.CONTENT_URI), 85);
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            Log.e("MainActivity", "Failed to pick contact");
        } else if (requestCode == 85) {
            contactPicked(data);
        }
    }

    private void contactPicked(Intent data) {
        try {
            Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
            cursor.moveToFirst();
            int phoneIndex = cursor.getColumnIndex("data1");
            int nameIndex = cursor.getColumnIndex("display_name");
            String phoneNo2 = cursor.getString(phoneIndex);
            cursor.getString(nameIndex);
            this.txtphoneNo.setText(phoneNo2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}