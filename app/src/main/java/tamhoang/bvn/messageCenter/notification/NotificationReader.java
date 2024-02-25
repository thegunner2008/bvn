package tamhoang.bvn.messageCenter.notification;

import static android.content.ContentValues.TAG;
import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.internal.view.SupportMenu;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import tamhoang.bvn.R;
import tamhoang.bvn.constants.Const;
import tamhoang.bvn.data.BaseStore;
import tamhoang.bvn.data.BusEvent;
import tamhoang.bvn.data.Contact;
import tamhoang.bvn.data.DbOpenHelper;
import tamhoang.bvn.data.model.KhachHang;
import tamhoang.bvn.data.model.TinNhanS;
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService;
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService;
import tamhoang.bvn.data.setting.Setting;
import tamhoang.bvn.data.store.KhachHangStore;
import tamhoang.bvn.data.store.TinNhanStore;
import tamhoang.bvn.messageCenter.telegram.TelegramHandle;
import tamhoang.bvn.ui.main.MainActivity;
import tamhoang.bvn.ui.main.MainState;
import tamhoang.bvn.util.Convert;
import tamhoang.bvn.util.ld.CongThuc;
import tamhoang.bvn.util.notifLib.models.Action;
import tamhoang.bvn.util.notifLib.utils.NotificationUtils;

public class NotificationReader extends NotificationListenerService {
    public static final String VIBER = "com.viber.voip";
    public static final String WHATSAPP = "com.whatsapp";
    public static final String ZALO = "com.zing.zalo";

    public static final String CHANNEL_ID = "ld_service";

    String ID = "";
    String Ten_KH;
    String body = "";
    JSONObject caidat_tg;
    Context context;
    DbOpenHelper db;
    JSONObject json;
    String mWhat = "";
    int soTN;

    public void onCreate() {
        super.onCreate();
        this.db = new DbOpenHelper(getBaseContext());
        startForegrounds();
    }

    @SuppressLint("RestrictedApi")
    static public void createNotification(String aMessage, Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);
        new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(Const.ALL_SMS_LOADER, 134217728);
        NotificationCompat.Builder nBuider = new NotificationCompat.Builder(context);
        nBuider.setContentTitle("LdPro");
        nBuider.setContentText(aMessage);
        nBuider.setSmallIcon(R.drawable.icon);
        nBuider.setContentIntent(pendingIntent);
        nBuider.setDefaults(1);
        nBuider.setVibrate(new long[]{100, 2000, 500, 2000});
        nBuider.setLights(-16711936, 400, 400);
        @SuppressLint("WrongConstant") NotificationManager mNotificationManager = (NotificationManager) context.getSystemService("notification");
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("10001", "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 2000, 500, 2000});
            nBuider.setChannelId("10001");
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(0, nBuider.build());

        EventBus.getDefault().post(new BusEvent.SetupErrorBagde(0));
    }

    private void startForegrounds() {
        createNotificationChannel();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.change)
                .setContentTitle("LdPro - Đang chạy")
                .setPriority(PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE).build();
        startForeground(101, notification);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Ld Background Service", NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!sbn.getPackageName().equals(ZALO) && !sbn.getPackageName().equals(WHATSAPP) && !sbn.getPackageName().equals(VIBER))
            return;
        if (this.context == null) this.context = this;

        Bundle extras = sbn.getNotification().extras;
        if (extras.getCharSequence(NotificationCompat.EXTRA_TEXT) == null) return;
        String text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString();//nội dung tin nhắn hoặc số(tin nhắn)

        if (!text.isEmpty()) processText(sbn, text, 1, 1);
    }

    public void processText(StatusBarNotification sbn, String text, int process, int number) {
        String app = null;
        String GhepTen; // app - tên KH
        Iterator<Notification.Action> it;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
        dmyFormat.setTimeZone(TimeZone.getDefault());
        hourFormat.setTimeZone(TimeZone.getDefault());
        String mNgayNhan = dmyFormat.format(calendar.getTime());
        String mGionhan = hourFormat.format(calendar.getTime());
        try {
            this.ID = "";
            Bundle extras = sbn.getNotification().extras;
            Notification notification = sbn.getNotification();
            String title = extras.getCharSequence(NotificationCompat.EXTRA_TITLE).toString();// 1/ = Tên KH (số tin nhắn)

            if (sbn.getPackageName().equals(ZALO)) {
                if (!text.contains("tin nhắn chưa đọc.") && !text.contains("đã gửi tập tin cho bạn") && !text.contains("cảm xúc với tin nhắn bạn gửi")
                        && !text.contains("hình động cho bạn") && !text.contains("thành viên của nhóm") && !text.contains("thêm vào nhóm")
                        && !text.contains("gửi ảnh cho bạn") && !text.contains("cuộc trò chuyện") && !text.contains("ang gọi đến")) {
                    this.ID = title;
                    if (title.contains(" (")) {
                        this.ID = this.ID.substring(0, this.ID.indexOf("(")).trim(); // 2/ = Tên người nhắn
                    }

                    if (title.indexOf("Nhóm") == 0) {
                        this.ID = this.ID.substring(this.ID.indexOf(":") + 2).trim();
                        text = text.substring(text.indexOf(":") + 1).trim();
                    }
                    text = text.replaceAll("'", "");
                    app = "ZL";
                }
            }

            if (sbn.getPackageName().equals(VIBER) && !text.contains("Bạn có các tin nhắn") && !text.contains("thêm bạn vào")
                    && !text.contains("uộc gọi") && !text.contains("tin nhắn chưa đọc") && !title.endsWith("Tôi")) {
                this.ID = title;
                if (this.ID.contains("trong")) {
                    this.ID = this.ID.substring(this.ID.indexOf("trong") + 6);
                }
                if (!this.ID.contains("tin nhắn chưa đọc"))
                    app = "VB";
            }

            if (sbn.getPackageName().equals(WHATSAPP) && !text.contains("tin nhắn")) {
                this.ID = text;
                if (text.contains(":")) {
                    this.ID = this.ID.substring(0, this.ID.indexOf(":")).trim();
                    this.mWhat = title.substring(title.indexOf(":") + 1).trim();
                }
                if (this.ID.contains("@")) {
                    this.mWhat = this.ID.substring(0, this.ID.indexOf("@"));
                    this.ID = this.ID.substring(this.ID.indexOf("@") + 1).trim();
                }
                if (this.ID.contains(" (")) {
                    this.ID = this.ID.substring(0, this.ID.indexOf("(")).trim();
                }
                this.ID = Convert.convertToLatin(this.ID.trim());

                for (int i2 = 0; i2 < this.ID.length(); i2++) {
                    if (this.ID.charAt(i2) > 127 || this.ID.charAt(i2) < 31) {
                        this.ID = this.ID.substring(0, i2) + this.ID.substring(i2 + 1);
                    }
                }
                if (text.contains(this.mWhat.trim()) || this.mWhat.length() <= 0) {
                    this.mWhat = text;
                    app = "WA";
                }
            }

            GhepTen = app + " - " + this.ID.trim();
            if (app != null) {
                JSONObject jsonTin;
                if (MainState.Json_Tinnhan.has(GhepTen)) {
                    jsonTin = new JSONObject(MainState.Json_Tinnhan.getString(GhepTen));

                    if (jsonTin.has(text.trim())) { // nếu KH đã có trong danh sách && trùng tin
                        app = null;
                    } else {
                        jsonTin.put(text.trim(), "OK"); //{tin nhắn : OK}
                        MainState.Json_Tinnhan.put(GhepTen, jsonTin.toString());
                    }
                } else {
                    jsonTin = new JSONObject();
                    jsonTin.put(text.trim(), "OK"); //{tin nhắn : OK}
                    MainState.Json_Tinnhan.put(GhepTen, jsonTin.toString());
                }
            }

            if (!GhepTen.contains("null")) {
                Notification.WearableExtender wearableExtender = new Notification.WearableExtender(notification);
                it = wearableExtender.getActions().iterator();
                while (it.hasNext()) {
                    Notification.Action act = it.next();
                    if (act.title.toString().contains("Trả lời") || act.title.toString().contains("Reply")) {
                        Action action = NotificationUtils.getQuickReplyAction(sbn.getNotification(), getPackageName());
                        if (MainState.contactsMap.containsKey(GhepTen)) {
                            Contact cont = MainState.contactsMap.get(GhepTen);
                            if (cont != null) {
                                cont.process = process;
                                cont.number = number;
                                cont.name = GhepTen;
                                cont.app = app;
                                cont.pendingIntent = act.actionIntent;
                                cont.remoteInput = act.getRemoteInputs()[0];
                                cont.remoteExtras = sbn.getNotification().extras;
                                cont.action = action;
                            }
                        } else {
                            Contact cont = new Contact();
                            cont.process = process;
                            cont.number = number;
                            cont.name = GhepTen;
                            cont.app = app;
                            cont.pendingIntent = act.actionIntent;
                            cont.remoteInput = act.getRemoteInputs()[0];
                            cont.remoteExtras = sbn.getNotification().extras;
                            cont.action = NotificationUtils.getQuickReplyAction(sbn.getNotification(), getPackageName());

                            MainState.contactsMap.put(GhepTen, cont);
                        }
                        if (MainState.mNotifi == null) MainState.mNotifi = this;

                    }
                }
            }

            if (text.toLowerCase().contains("ldpro") && text.trim().length() == 5) {
                MainState.mNotifi = this;
                reply(GhepTen, "Tin mồi!");
            }

            if (app != null) {
                try {
                    if (!GhepTen.contains("null")) {
                        String query = "Select * From Chat_database WHERE " +
                                "ngay_nhan = '" + mNgayNhan + "' And Ten_kh = '" + GhepTen + "' AND nd_goc = '" + text + "'";
                        Cursor cursor = db.getData(query);

                        if (cursor.getCount() == 0) {//neu khong bi trung tin nhan

                            String queryInsert = "Insert into Chat_database Values" +
                                    "( null,'" + mNgayNhan + "', '" + mGionhan + "', 1, '" + GhepTen + "', '" + GhepTen + "', '" + app + "','" + text + "',1)";
                            try {
                                this.db.queryData(queryInsert);
                                TelegramHandle.sms = true;
                            } catch (Exception e) {
                                return;
                            }
                        } else {
                            return;
                        }

                        cursor.close();
                        if (!GhepTen.contains("null") && text.length() > 5) {
                            this.body = text.replaceAll("'", " ");
                            this.Ten_KH = GhepTen;
                            if (!(!MainState.DSkhachhang.contains(GhepTen) || this.body.startsWith("Ok") || this.body.startsWith("Bỏ")
                                    || this.body.toLowerCase().startsWith("ldpro") || this.body.startsWith("Thiếu")
                                    || this.body.startsWith("Success")) || this.body.contains("Tra lai")) {

                                KhachHang khachHang_s = KhachHangStore.I.selectByName(Ten_KH);
                                json = new JSONObject(khachHang_s.getTbl_MB());
                                caidat_tg = json.getJSONObject("caidat_tg");

                                if (!CongThuc.checkTime(caidat_tg.getString("tg_debc"))) {
                                    int maxSoTn = BaseStore.INSTANCE.getMaxSoTinNhan(mNgayNhan, 1, "ten_kh = '" + Ten_KH + "'");
                                    soTN = maxSoTn + 1;

                                    boolean isTraLai = body.contains("Tra lai");
                                    int okTn = isTraLai ? 0 : 1;
                                    TinNhanStore.I.insert(
                                            new TinNhanS(null, mNgayNhan, mGionhan, 1, Ten_KH, khachHang_s.getSdt(), app,
                                                    soTN, body, null, body, "ko", 0, okTn, okTn, null)
                                    );

                                    if (MainState.checkHSD()) {
                                        TinNhanS tinNhanS_g = TinNhanStore.I.select(mNgayNhan, Ten_KH, soTN, 1);
                                        try {
                                            XuLyTinNhanService.I.upsertTinNhan(tinNhanS_g.getID(), 1);
                                        } catch (Exception e) {
                                            db.queryData("Update tbl_tinnhanS set phat_hien_loi = 'ko' WHERE id = " + tinNhanS_g.getID());
                                            db.queryData("Delete From tbl_soctS WHERE ngay_nhan = '" + mNgayNhan + "' AND ten_kh = '" + this.Ten_KH + "' AND so_tin_nhan = " + this.soTN + " AND type_kh = 1");
                                        } catch (Throwable throwable) {
                                            Log.e(TAG, "onNotificationPosted: throwable " + throwable);
                                        }
                                        if (!CongThuc.checkTime("18:30") && !isTraLai) {
                                            Log.e(TAG, "onNotificationPosted: !CheckTime(18:30) " + MainState.jsonTinnhan.has(Ten_KH) + " - " + Ten_KH);
                                            if (MainState.handler == null) {
                                                MainState.handler = new Handler();
                                                MainState.handler.postDelayed(MainState.runnable, 1000);
                                            }
                                            if (!MainState.jsonTinnhan.has(Ten_KH)) {
                                                JSONObject jsontinnan = new JSONObject();
                                                jsontinnan.put("Time", 0);
                                                MainState.jsonTinnhan.put(Ten_KH, jsontinnan.toString());
                                            } else {
                                                JSONObject jsontinnan = new JSONObject(MainState.jsonTinnhan.getString(Ten_KH));
                                                jsontinnan.put("Time", 0);
                                                MainState.jsonTinnhan.put(Ten_KH, jsontinnan.toString());
                                            }

                                            GuiTinNhanService.I.replyKhach(tinNhanS_g.getID());
                                        }

                                    }

                                } else {
                                    int maxSoTn = BaseStore.INSTANCE.getMaxSoTinNhan(mNgayNhan, null, "ten_kh = '" + Ten_KH + "'");
                                    soTN = maxSoTn + 1;

                                    TinNhanS tinNhanS_i = new TinNhanS(null, mNgayNhan, mGionhan, 1, Ten_KH, khachHang_s.getSdt(), app,
                                            soTN, body, null, body, "Hết giờ nhận số!", 0, 1, 1, null);
                                    TinNhanStore.I.insert(tinNhanS_i);

                                    if (!CongThuc.checkTime("18:30") && Setting.I.getJson().getInt("tin_qua_gio") == 1) {
                                        reply(this.Ten_KH, "Hết giờ nhận!");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "onNotificationPosted: error " + e.getMessage());
        }
    }

    public void reply(String mName, String message) {
        Contact contact = MainState.contactsMap.get(mName);
        if (contact != null) {
            try {
                if (contact.remoteInput == null && contact.name.contains("ZL - ")) {
                    contact.action.sendReply(MainState.mNotifi, message);
                } else {
                    Intent intent = new Intent();
                    Bundle bundle = contact.remoteExtras;

                    RemoteInput[] remoteInputArr = {contact.remoteInput};

                    bundle.putCharSequence(remoteInputArr[0].getResultKey(), message);
                    RemoteInput.addResultsToIntent(remoteInputArr, intent, bundle);

                    contact.pendingIntent.send(MainState.mNotifi, 0, intent);
                }

                if (MainState.Json_Tinnhan.has(mName)) MainState.Json_Tinnhan.remove(mName);

            } catch (Exception e) {
                Log.e(TAG, "onNotificationPosted: error " + e);
                e.printStackTrace();
            }
        }
    }
}