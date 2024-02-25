package tamhoang.bvn.akaman;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Keep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kotlin.UByte;
import tamhoang.bvn.data.DbOpenHelper;
import tamhoang.bvn.util.Util;

@Keep
public class AkaManSec {
    public static boolean akaEnable;
    public static String encryptString;
    public static int pwdMode;
    public static String resetPwd;
    public static String separator;
    public static int truncateMode;
    public static String truncatePwd;
    public static int useTruncate;
    public static String userPwd;

    public static native String getAkaCipher();

    public static native String getAkaS();

    public static native String getAlgorithm();

    public static native String getCharsetName();

    static {
        System.loadLibrary("akces");
        pwdMode = 0;
        truncateMode = 0;
        useTruncate = 0;
        akaEnable = true;
        separator = ",";
    }

    public static String readKeyFile() {
        String str;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(getAkaMainPath()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            StringBuilder sb = new StringBuilder();
            boolean z = false;
            while (!z) {
                try {
                    str = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    str = null;
                }
                boolean z2 = str == null;
                if (str != null) {
                    sb.append(str);
                }
                z = z2;
            }
            try {
                bufferedReader.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            fileInputStream.close();
            return sb.toString();
        } catch (Exception e3) {
            e3.printStackTrace();
            Log.w("AkaSec", "Something went Wrong");
            return null;
        }
    }

    public static String getAkaManSec(String fileEncriptContent) {
        try {
            byte[] decode = Base64.decode(fileEncriptContent, 0);
            if (decode.length < 17) {
                Log.w("AkaSec", "Something went Wrong");
            }
            byte[] copyOfRange = Arrays.copyOfRange(decode, 0, 16);
            byte[] copyOfRange2 = Arrays.copyOfRange(decode, 16, decode.length);
            Cipher cipher = Cipher.getInstance(getAkaCipher());
            cipher.init(2, new SecretKeySpec(getAkaS().getBytes(getCharsetName()), getAlgorithm()), new IvParameterSpec(copyOfRange, 0, cipher.getBlockSize()));
            return new String(cipher.doFinal(copyOfRange2));
        } catch (UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void initSecTable(DbOpenHelper db) {
        db.queryData("CREATE TABLE IF NOT EXISTS tbl_active(ID INTEGER PRIMARY KEY AUTOINCREMENT, encrypt_string TEXT, user_pwd TEXT, reset_pwd TEXT, truncate_pwd TEXT, truncate_mode INTEGER DEFAULT 0, pwd_mode INTEGER DEFAULT 0, use_truncate INTEGER DEFAULT 0)");
    }

    public static void saveAkaManSec(String content, DbOpenHelper db) {
        userPwd = "";
        truncatePwd = "";
        pwdMode = 0;
        truncateMode = 0;
        useTruncate = 0;
        db.queryData("DELETE FROM tbl_active");
        db.queryData("INSERT Into tbl_active (encrypt_string, user_pwd, reset_pwd, truncate_pwd, truncate_mode, pwd_mode, use_truncate) Values ('" + content + "', '" + userPwd + "', '" + resetPwd + "', '" + truncatePwd + "', " + truncateMode + ", " + pwdMode + ", " + useTruncate + ")");
    }

    public static void updateAkaManSec(DbOpenHelper db) {
        db.queryData("DELETE FROM tbl_active");
        db.queryData("INSERT Into tbl_active (encrypt_string, user_pwd, reset_pwd, truncate_pwd, truncate_mode, pwd_mode, use_truncate) Values ('" + encryptString + "', '" + userPwd + "', '" + resetPwd + "', '" + truncatePwd + "', " + truncateMode + ", " + pwdMode + ", " + useTruncate + ")");
    }

    public static void queryAkaManPwd(DbOpenHelper db) {
        try {
            Cursor GetData = db.getData("Select user_pwd, reset_pwd, truncate_pwd, pwd_mode, truncate_mode, encrypt_string, use_truncate From tbl_active LIMIT 1;");
            try {
                if (GetData.moveToNext()) {
                    userPwd = GetData.getString(0);
                    resetPwd = GetData.getString(1);
                    truncatePwd = GetData.getString(2);
                    pwdMode = GetData.getInt(3);
                    truncateMode = GetData.getInt(4);
                    encryptString = GetData.getString(5);
                    useTruncate = GetData.getInt(6);
                }
                if (GetData.isClosed()) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Util.writeLog(e);
                if (GetData == null || GetData.isClosed()) {
                    return;
                }
            }
            GetData.close();
        } catch (SQLException e) {
            userPwd = "";
        }
    }

    public static String queryAkaManSec(DbOpenHelper db) {
        String str;
        str = "";
        Cursor GetData = db.getData("Select encrypt_string From tbl_active LIMIT 1;");
        try {
            try {
                str = GetData.moveToNext() ? GetData.getString(0) : "";
            } catch (Exception e) {
                e.printStackTrace();
                Util.writeLog(e);
            }
            return str;
        } finally {
            if (GetData != null && !GetData.isClosed()) {
                GetData.close();
            }
        }
    }

    public static String md5(final String s) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(s.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hexString = Integer.toHexString(b & UByte.MAX_VALUE);
                while (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAkaMainPath() {
        return Environment.getExternalStorageDirectory() + File.separator + "keys.txt";
    }
}
