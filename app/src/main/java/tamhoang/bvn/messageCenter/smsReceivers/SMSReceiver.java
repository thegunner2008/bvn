package tamhoang.bvn.messageCenter.smsReceivers;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import tamhoang.bvn.R;
import tamhoang.bvn.data.BaseStore;
import tamhoang.bvn.data.DbOpenHelper;
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService;
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService;
import tamhoang.bvn.messageCenter.smsServices.SaveSmsService;
import tamhoang.bvn.messageCenter.telegram.TelegramHandle;
import tamhoang.bvn.ui.main.MainState;
import tamhoang.bvn.util.ld.CongThuc;

public class SMSReceiver extends BroadcastReceiver {
    String Ten_KH;
    String body = "";
    JSONObject caidat_gia;
    JSONObject caidat_tg;
    DbOpenHelper db;
    JSONObject json;
    String mGionhan;
    String mNgayNhan;
    String mSDT;
    SmsMessage[] messages = null;
    int soTN;

    public void onReceive(Context context, Intent intent) {
        this.db = new DbOpenHelper(context);
        boolean ktra_trungtin = true;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            this.messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                this.messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                SmsMessage currentSMS = getIncomingMessage(pdus[i], bundle);
                issueNotification(context, currentSMS.getDisplayOriginatingAddress(), currentSMS.getDisplayMessageBody());
                saveSmsInInbox(context, currentSMS);
            }
            SmsMessage sms = this.messages[0];
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
            dmyFormat.setTimeZone(TimeZone.getDefault());
            hourFormat.setTimeZone(TimeZone.getDefault());
            this.mNgayNhan = dmyFormat.format(calendar.getTime());
            this.mGionhan = hourFormat.format(calendar.getTime());
            this.mSDT = "";
            try {
                if (this.messages.length > 0 && !sms.isReplace()) {
                    StringBuilder bodyText = new StringBuilder();
                    for (SmsMessage message : this.messages) {
                        bodyText.append(message.getMessageBody());
                    }
                    this.body = bodyText.toString().replace("'", "");
                    this.mSDT = sms.getDisplayOriginatingAddress().replace(" ", "").trim();
                    if (this.mSDT.startsWith("0")) {
                        this.mSDT = "+84" + this.mSDT.substring(1);
                    }
                    if (MainState.DSkhachhang.size() == 0) {
                        MainState.refreshDsKhachHang();
                    }
                    if ((MainState.DSkhachhang.contains(this.mSDT)
                            || this.body.indexOf("Ok") == 0
                            || this.body.indexOf("Bỏ") == 0
                            || this.body.indexOf("Thiếu") == 0)
                            || this.body.contains("Tra lai")) {
                        TelegramHandle.sms = true;
                        try {
                            if (MainState.jSon_Setting.getInt("tin_trung") > 0) {
                                Cursor Ktratin = this.db.getData("Select id From tbl_tinnhanS WHERE so_dienthoai = '" + this.mSDT + "' AND ngay_nhan = '" + this.mNgayNhan + "' AND nd_goc = '" + this.body + "'");
                                Ktratin.moveToFirst();
                                if (Ktratin.getCount() > 0) {
                                    ktra_trungtin = false;
                                }
                                if (!Ktratin.isClosed()) {
                                    Ktratin.close();
                                }
                            }
                        } catch (JSONException e2) {
                            Log.d(SMSReceiver.class.getName(), e2.getMessage());
                        }
                        try {
                            Cursor getTenKH = this.db.getData("Select * FROM tbl_kh_new WHERE sdt ='" + this.mSDT + "'");
                            getTenKH.moveToFirst();
                            if (ktra_trungtin) {
                                try {
                                    JSONObject jSONObject = new JSONObject(getTenKH.getString(5));
                                    this.json = jSONObject;
                                    this.caidat_gia = jSONObject.getJSONObject("caidat_gia");
                                    this.caidat_tg = this.json.getJSONObject("caidat_tg");

                                    if (!CongThuc.checkTime(this.caidat_tg.getString("tg_debc"))) {

                                        int maxSoTn = BaseStore.INSTANCE.getMaxSoTinNhan(mNgayNhan, 1, "so_dienthoai = '" + mSDT + "'");

                                        this.Ten_KH = getTenKH.getString(0);
                                        this.soTN = maxSoTn + 1;
                                        this.db.queryData(!this.body.contains("Tra lai") ?
                                                "Insert Into tbl_tinnhanS values (null, '" + this.mNgayNhan + "', '" + this.mGionhan + "',1, '" + this.Ten_KH + "', '" + getTenKH.getString(1) + "','sms', " + this.soTN + ", '" + this.body + "',null,'" + this.body + "', 'ko',0,1,1, null)"
                                                : "Insert Into tbl_tinnhanS values (null, '" + this.mNgayNhan + "', '" + this.mGionhan + "',1, '" + this.Ten_KH + "', '" + getTenKH.getString(1) + "','sms', " + this.soTN + ", '" + this.body + "',null,'" + this.body + "', 'ko',0,0,0, null)");
                                        if (MainState.checkHSD()) {
                                            Cursor c = this.db.getData("Select * from tbl_tinnhanS WHERE ngay_nhan = '" + this.mNgayNhan + "' AND so_dienthoai = '" + this.mSDT + "' AND so_tin_nhan = " + this.soTN + " AND type_kh = 1");
                                            c.moveToFirst();
                                            XuLyTinNhanService.I.upsertTinNhan(c.getInt(0), 1);
                                            if (!CongThuc.checkTime("18:30") && !this.body.contains("Tra lai")) {
                                                GuiTinNhanService.I.replyKhach((c.getInt(0)));
                                            }
                                            c.close();
                                        }
                                    } else {
                                        int maxSoTn = BaseStore.INSTANCE.getMaxSoTinNhan(mNgayNhan, 1, "so_dienthoai = '" + mSDT + "'");

                                        this.Ten_KH = getTenKH.getString(0);
                                        this.soTN = maxSoTn + 1;
                                        this.db.queryData("Insert Into tbl_tinnhanS values (null, '" + this.mNgayNhan + "', '" + this.mGionhan + "',1, '" + this.Ten_KH + "', '" + getTenKH.getString(1) + "','sms', " + this.soTN + ", '" + this.body + "',null,'" + this.body + "', 'Hết giờ nhận số!',0,1,1, null)");
                                        if (!CongThuc.checkTime("18:30") && MainState.jSon_Setting.getInt("tin_qua_gio") == 1) {
                                            GuiTinNhanService.I.sendSMS(getTenKH.getString(1), "Hết giờ nhận!");
                                        }
                                    }

                                } catch (JSONException e3) {
                                    Log.d(SMSReceiver.class.getName(), e3.getMessage());
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                            if (!getTenKH.isClosed()) {
                                getTenKH.close();
                            }
                            return;
                        } catch (Exception e10) {
                            Log.d(SMSReceiver.class.getName(), e10.getMessage());
                            return;
                        }
                    }
                }
                this.body = sms.getDisplayMessageBody().replace("'", "");
                this.mSDT = sms.getDisplayOriginatingAddress().replace(" ", "").trim();
            } catch (Exception e13) {
                Log.d(SMSReceiver.class.getName(), e13.getMessage());
            }
        }
    }

    private void saveSmsInInbox(Context context, SmsMessage sms) {
        Intent serviceIntent = new Intent(context, SaveSmsService.class);
        serviceIntent.putExtra("sender_no", sms.getDisplayOriginatingAddress());
        serviceIntent.putExtra("message", sms.getDisplayMessageBody());
        serviceIntent.putExtra("date", sms.getTimestampMillis());
        context.startService(serviceIntent);
    }

    @SuppressLint("WrongConstant")
    private void issueNotification(Context context, String senderNo, String message) {
        ((NotificationManager) context.getSystemService("notification")).notify(101, new NotificationCompat.Builder(context).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher)).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(senderNo).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setAutoCancel(true).setContentText(message).build());
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        return SmsMessage.createFromPdu((byte[]) aObject, bundle.getString("format"));
    }
}