package tamhoang.bvn.messageCenter.telegram;

import static tamhoang.bvn.util.ld.CongThuc.checkIsToday;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Keep;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
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
import tamhoang.bvn.ui.main.MainState;
import tamhoang.bvn.util.ld.CongThuc;

@Keep
public class TelegramHandle {
    public static Client client;
    public static JSONObject Json_Chat_Telegram = new JSONObject();
    public String firstNameTL = "";
    public String lastNameTL = "";
    public static String my_id = "";
    public static boolean sms = false;

    public TelegramHandle(TelegramClient.Callback callback) {
        client = TelegramClient.getClient(callback);
        client.send(new TdApi.GetMe(), callback);
    }

    public void onResult(TdApi.Object object, DbOpenHelper db, Context applicationContext, Context context, TelegramClient.Callback callback) {
        boolean tinHethong;
        String ten_kh;
        int type_kh;
        Cursor cursor;
        switch (object.getConstructor()) {
            case TdApi.User.CONSTRUCTOR:
                my_id = ((TdApi.User) object).id + "";
                break;
            case TdApi.UpdateNewMessage.CONSTRUCTOR:
                break;
            case TdApi.UpdateOption.CONSTRUCTOR:
                TdApi.UpdateOption updateOption = (TdApi.UpdateOption) object;
                if (updateOption.name.contains("my_id")) {
                    String optionValue = updateOption.value.toString();
                    String substring = optionValue.substring(optionValue.indexOf("=") + 1);
                    my_id = substring.substring(0, substring.indexOf("\n")).trim();
                    db.queryData("Update So_Om set Sphu1 = '" + my_id + "' WHERE ID = 1");
                    return;
                }
                return;
            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                try {
                    if (!Json_Chat_Telegram.has(updateUser.user.id + "")) {
                        JSONObject json = new JSONObject();
                        String type = updateUser.user.type.toString();
                        json.put("type", type.substring(0, type.indexOf("{")).trim());
                        json.put("basicGroupId", updateUser.user.id);
                        firstNameTL = updateUser.user.firstName;
                        lastNameTL = updateUser.user.lastName;
                        json.put("title", "TL - " + firstNameTL + " " + lastNameTL);
                        Json_Chat_Telegram.put(updateUser.user.id + "", json);
                        return;
                    }
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            case TdApi.UpdateConnectionState.CONSTRUCTOR:
                if (((TdApi.UpdateConnectionState) object).state.getConstructor() == TdApi.ConnectionStateReady.CONSTRUCTOR) {
                    Log.d("AuthActivity", "onResult: ConnectionStateReady");
                    return;
                }
                return;
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                onAuthStateUpdated(applicationContext, context, callback, ((TdApi.UpdateAuthorizationState) object).authorizationState);
                return;
            case TdApi.UpdateNewChat.CONSTRUCTOR:
                TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                try {
                    if (!Json_Chat_Telegram.has(updateNewChat.chat.id + "")) {
                        JSONObject json = new JSONObject();
                        String type2 = updateNewChat.chat.type.toString();
                        json.put("type", type2.substring(0, type2.indexOf("{")).trim());
                        json.put("basicGroupId", updateNewChat.chat.id);
                        json.put("title", "TL - " + updateNewChat.chat.title);
                        Json_Chat_Telegram.put(updateNewChat.chat.id + "", json);
                        return;
                    }
                    return;
                } catch (JSONException e2) {
                    e2.printStackTrace();
                    return;
                }
            default:
                return;
        }
        if (my_id == "") {
            Cursor cursor2 = db.getData("Select Sphu1 from so_om where ID = 1");
            cursor2.moveToFirst();
            my_id = cursor2.getString(0);
            cursor2.close();
        }
        assert object instanceof TdApi.UpdateNewMessage;
        TdApi.UpdateNewMessage newMessage = (TdApi.UpdateNewMessage) object;
        String senderUserId = newMessage.message.senderId + "";
        String chatId = newMessage.message.chatId + "";
        String text = ((TdApi.MessageText) newMessage.message.content).text.text.replace("'", "");
        tinHethong = !newMessage.message.isChannelPost && newMessage.message.chatId != 777000 && newMessage.message.chatId != 93372553;
        if (tinHethong) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat dmyFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
            dmyFormat.setTimeZone(TimeZone.getDefault());
            hourFormat.setTimeZone(TimeZone.getDefault());
            Date time = new Date((long) newMessage.message.date * 1000);
            String mNgayNhan = dmyFormat.format(time);
            String mGionhan = hourFormat.format(calendar.getTime());

            try {
                ten_kh = Json_Chat_Telegram.getJSONObject(chatId).getString("title");
            } catch (JSONException e3) {
                Cursor cursor3 = db.getData("Select * From tbl_kh_new Where sdt = '" + chatId + "'");
                if (cursor3.getCount() > 0) {
                    cursor3.moveToFirst();
                    ten_kh = cursor3.getString(0);
                    cursor3.close();
                } else {
                    ten_kh = "TL - " + chatId;
                }
            }
            if (chatId.contains(my_id) || senderUserId.contains(my_id)) {
                type_kh = 2;
            } else {
                type_kh = 1;
            }
            db.queryData("Insert into Chat_database Values( null,'" + mNgayNhan + "', '" + mGionhan + "', "
                    + type_kh + ", '" + ten_kh + "','" + chatId + "', 'TL','" + text + "',1)");
            sms = true;
            String sb2 = "Select * From tbl_tinnhanS WHERE ngay_nhan = '" + mNgayNhan +
                    "' And Ten_kh = '" + ten_kh + "' AND nd_goc = '" + text + "'";
            Cursor cursor111 = db.getData(sb2);
            if (cursor111.getCount() == 0) {
                cursor = db.getData("Select * From tbl_kh_new Where sdt = '" + chatId + "'");
                if (cursor.getCount() > 0 && text.length() > 5 && checkIsToday(time)) {
                    cursor.moveToFirst();
                    if (cursor.getInt(3) == 1 && type_kh == 1) {
                        Xulytin(db, chatId, text, mNgayNhan, mGionhan, type_kh);
                        return;
                    }
                    if (cursor.getInt(3) == 2 && type_kh == 1 && text.indexOf("Tra lai") == 0) {
                        Xulytin(db, chatId, text, mNgayNhan, mGionhan, type_kh);
                        return;
                    }
                    if (cursor.getInt(3) == 3 && type_kh == 1) {
                        Xulytin(db, chatId, text, mNgayNhan, mGionhan, type_kh);
                        return;
                    }
                }
                cursor.close();
            }
            cursor111.close();
        }
    }

    private void onAuthStateUpdated(Context applicationContext, Context context, TelegramClient.Callback callback, TdApi.AuthorizationState authorizationState) {
        int constructor = authorizationState.getConstructor();

        //TODO: fake constructor
        if (constructor == TdApi.AuthorizationStateWaitCode.CONSTRUCTOR) {
            new Handler(Looper.getMainLooper()).post(() ->
            {
                if (!((Activity) context).isFinishing())
                    showInputCodeDialog(context, callback);
            });
        } else if (constructor == TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR) {
            client.send(new TdApi.CheckDatabaseEncryptionKey(), callback);
        } else if (constructor == TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR) {
            TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
            authStateRequest.apiId = 10464015;
            authStateRequest.apiHash = "1f1b9d36ea61c12e771746fc17120173";
            authStateRequest.useMessageDatabase = true;
            authStateRequest.useSecretChats = true;
            authStateRequest.systemLanguageCode = "en";
            authStateRequest.databaseDirectory = applicationContext.getFilesDir().getAbsolutePath();
            authStateRequest.deviceModel = "Moto";
            authStateRequest.systemVersion = "9.0";
            authStateRequest.applicationVersion = "0.1";
            authStateRequest.enableStorageOptimizer = true;
            client.send(new TdApi.SetTdlibParameters(authStateRequest), callback);
        }
    }

    private void Xulytin(DbOpenHelper db, String mSDT, String body, String mNgayNhan, String mGionhan, int type_kh) {
        if ((MainState.DSkhachhang.contains(mSDT) && body.indexOf("Ok") != 0 && body.indexOf("Bỏ") != 0 && body.indexOf("Thiếu") != 0) || body.contains("Tra lai")) {
            sms = true;
            JSONObject json = null;
            JSONObject caidat_tg = null;
            Cursor getTenKH = db.getData("Select * FROM tbl_kh_new WHERE sdt ='" + mSDT + "'");
            getTenKH.moveToFirst();
            try {
                json = new JSONObject(getTenKH.getString(5));
                caidat_tg = json.getJSONObject("caidat_tg");
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                if (!CongThuc.checkTime(caidat_tg.getString("tg_debc"))) {
                    try {
                        int maxSoTn = BaseStore.INSTANCE.getMaxSoTinNhan(mNgayNhan, 1, "so_dienthoai = '" + mSDT + "'");

                        String Ten_KH = getTenKH.getString(0);
                        int soTN = maxSoTn + 1;
                        if (!body.contains("Tra lai")) {
                            try {
                                String qr = "Insert Into tbl_tinnhanS values (null, '" + mNgayNhan + "', '" + mGionhan + "'," + type_kh + ", '" + Ten_KH + "', '" + getTenKH.getString(1) + "','TL', " + soTN + ", '" + body + "',null,'" + body + "', 'ko',0,1,1, null)";
                                db.queryData(qr);
                            } catch (SQLException e) {
                            }
                        } else {
                            try {
                                String qr = "Insert Into tbl_tinnhanS values (null, '" + mNgayNhan + "', '" + mGionhan + "'," + type_kh + ", '" + Ten_KH + "', '" + getTenKH.getString(1) + "','TL', " + soTN + ", '" + body + "',null,'" + body + "', 'ko',0,0,0, null)";
                                db.queryData(qr);
                            } catch (SQLException e) {
                            }
                        }

                        if (MainState.checkHSD()) {
                            String sb = "Select * from tbl_tinnhanS WHERE ngay_nhan = '" + mNgayNhan +
                                    "' AND so_dienthoai = '" + mSDT +
                                    "' AND so_tin_nhan = " + soTN +
                                    " AND type_kh = " + type_kh;
                            Cursor c = db.getData(sb);
                            c.moveToFirst();
                            try {
                                XuLyTinNhanService.I.upsertTinNhan(c.getInt(0), 1);
                            } catch (Exception e7) {
                                db.queryData("Update tbl_tinnhanS set phat_hien_loi = 'ko' WHERE id = " + c.getInt(0));
                                db.queryData("Delete From tbl_soctS WHERE ngay_nhan = '" + mNgayNhan + "' AND so_dienthoai = '" + mSDT + "' AND so_tin_nhan = " + soTN + " AND type_kh =" + type_kh);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (!CongThuc.checkTime("18:30") && !body.contains("Tra lai") && type_kh == 1) {
                                GuiTinNhanService.I.replyKhach((c.getInt(0)));
                            }
                            c.close();
                        }
                    } catch (SQLException e) {
                    }
                    if (!getTenKH.isClosed()) {
                        getTenKH.close();
                        return;
                    }
                }

                int maxSoTn = BaseStore.INSTANCE.getMaxSoTinNhan(mNgayNhan, 1, "so_dienthoai = '" + mSDT + "'");

                db.queryData("Insert Into tbl_tinnhanS values (null, '" + mNgayNhan + "', '" + mGionhan + "',1, '" + getTenKH.getString(0) + "', '" + getTenKH.getString(1) + "','TL', " + (maxSoTn + 1) + ", '" + body + "',null,'" + body + "', 'Hết giờ nhận số!',0,1,1, null)");

                if (!CongThuc.checkTime("18:30") && MainState.jSon_Setting.getInt("tin_qua_gio") == 1) {
                    sendMessage(getTenKH.getLong(1), "Hết giờ nhận!");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void showInputPhoneDialog(Context context, TelegramClient.Callback callback) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_tele_login);
        dialog.getWindow().setLayout(-1, -2);
        final EditText authPhone = (EditText) dialog.findViewById(R.id.authPhone);
        ((Button) dialog.findViewById(R.id.loginBtn)).setOnClickListener(view -> {
            String PhoneNumber = authPhone.getText().toString();
            if (PhoneNumber.length() == 10) {
                client = TelegramClient.getClient(callback);
                client.send(new TdApi.SetAuthenticationPhoneNumber("+84" + PhoneNumber.substring(1), null), callback);
                dialog.dismiss();
                return;
            }
            Toast.makeText(context, "Hãy nhập 10 số của số điện thoại!", Toast.LENGTH_SHORT).show();
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    public void showInputCodeDialog(Context context, TelegramClient.Callback callback) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_auth);
        dialog.getWindow().setLayout(-1, -2);
        final EditText authPhone = dialog.findViewById(R.id.authCode);
        dialog.findViewById(R.id.checkBtn).setOnClickListener(view -> {
            String code = authPhone.getText().toString();
            if (code.length() == 5) {
                client.send(new TdApi.CheckAuthenticationCode(code), callback);
                dialog.dismiss();
                return;
            }
            Toast.makeText(context, "Hãy nhập đủ 5 số được gửi về Telegram!", Toast.LENGTH_SHORT).show();
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    public static void sendMessage(long chatId, String message) {
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TelegramHandle.client.send(
                new TdApi.SendMessage(chatId, 0L, 0L, null,
                        new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row}),
                        new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true)),
                null);
    }

    public void logout(TelegramClient.Callback callback, DbOpenHelper db) {
        db.queryData("Update So_om set  Sphu1 ='' where ID = 1");
        if (client != null) client.send(new TdApi.LogOut(), callback, null);
        client = null;
        my_id = "";
    }
}
