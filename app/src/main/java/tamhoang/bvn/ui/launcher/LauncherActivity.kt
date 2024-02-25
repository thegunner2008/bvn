package tamhoang.bvn.ui.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.database.SQLException
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Xml
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import tamhoang.bvn.R
import tamhoang.bvn.akaman.AkaManSec
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.login.LoginActivity
import tamhoang.bvn.ui.main.MainActivity
import tamhoang.bvn.util.extensions.to2ChuSo
import java.io.FileNotFoundException
import java.io.IOException
import java.io.StringWriter
import java.util.*

class LauncherActivity : AppCompatActivity() {
    var db: DbOpenHelper? = null
    var btnLogin: Button? = null

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.R)
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_login)
        db = DbOpenHelper(this)
//        BaseStore.init(application)
        btnLogin = findViewById(R.id.btn_login)
        val check = intArrayOf(
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS),
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE),
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS),
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS),
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE),
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        )
        if (Arrays.stream(check).anyMatch { i: Int -> i == -1 }) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CALL_PHONE
                ), 1
            )
        }
        val reCheck = IntArray(7)
        btnLogin!!.setOnClickListener {
            reCheck[0] = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            reCheck[1] = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            reCheck[2] = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            reCheck[3] = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            reCheck[4] = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            reCheck[5] = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            reCheck[6] = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            if (!checkFileAccessPermission()) return@setOnClickListener
            if (Arrays.stream(reCheck).anyMatch { i: Int -> i != 0 }) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE
                    ), 1
                )
            } else if (getImei() != null) {
                btnLogin!!.text = "Đang khởi tạo dữ liệu..."
                createTableDatabase()
                val intent = Intent(this, MainActivity::class.java)
                startActivities(arrayOf(intent))
            }
        }
        try {
            createTableDatabase()
            AkaManSec.queryAkaManPwd(db)
            if (Arrays.stream(check).allMatch { i: Int -> i == 0 } && getImei() != null) {
                val pass = AkaManSec.userPwd
                val intent = if (pass == null || pass.isEmpty())
                    Intent(this, MainActivity::class.java)
                else
                    Intent(this, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                startActivities(arrayOf(intent))
            }
        } catch (ignored: SQLException) {
        }
    }

    @SuppressLint("WrongConstant", "HardwareIds")
    fun getImei(): String? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != -1) {
            try {
                Imei = (getSystemService("phone") as TelephonyManager).deviceId
                serial = Settings.Secure.getString(contentResolver, "android_id")
            } catch (ignored: Exception) {
            }
        }
        if (Imei != null) {
            val newSerializer = Xml.newSerializer()
            try {
                newSerializer.setOutput(StringWriter())
                newSerializer.startDocument("UTF-8", true)
                val openFileOutput = openFileOutput("new.xml", 0)
                openFileOutput.write(Imei!!.toByteArray(), 0, Imei!!.length)
                openFileOutput.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e1: Exception) {
                e1.printStackTrace()
                Toast.makeText(this, "Loi tao file", Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                val openFileInput = openFileInput("new.xml")
                Imei = ""
                while (true) {
                    val read = openFileInput.read()
                    if (read == -1) {
                        break
                    }
                    Imei += read.toChar()
                }
            } catch (e: FileNotFoundException) {
                checkDefaultSettings()
                e.printStackTrace()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return Imei
    }

    fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != 0
            && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)
        )
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 1)
    }

    private fun checkDefaultSettings() {
        val smsPackage = Telephony.Sms.getDefaultSmsPackage(applicationContext)
        if (smsPackage == null || smsPackage == applicationContext.packageName) {
            return
        }
        Dialog.simple(
            this,
            "Cài đặt mặc định!",
            "Để ứng dụng thành quản lý tin nhắn mặc định để quản lý tin nhắn tốt hơn!",
            positiveText = "Ok",
            positiveAction = {
                val setSmsAppIntent = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                    getSystemService(RoleManager::class.java).createRequestRoleIntent(RoleManager.ROLE_SMS)
                else
                    Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                        putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                    }

                startActivityForResult(setSmsAppIntent, 202)
            },
            negativeText = "Cancel",
            cancelable = false
        )
    }

    private fun checkFileAccessPermission(): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val getPermission = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(getPermission, 123)
                }
                return Environment.isExternalStorageManager()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun createTableDatabase() {
        db!!.apply {
            createTinNhanGoc()
            createTableChat()
            createSoCT()
            createSoOm()
            createKhachHang()
            createBangKQ()
            createThayThePhu()
            createAnotherSetting()
            creatChaytrangAcc()
            createChaytrangTicket()
        }
        AkaManSec.initSecTable(db)
        try {
            val cursor = db!!.getData("Select * From so_om")
            if (cursor.count < 1) {
                for (i in 0..99) {
                    db!!.queryData(
                        "Insert into so_om Values (null, '${i.to2ChuSo()}', 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null)"
                    )
                }
            }
            if (!cursor.isClosed) {
                cursor.close()
            }
        } catch (ignored: SQLException) {
        }
        try {
            val cursor = db!!.getData("Select Om_Xi3 FROM So_om WHERE So = '05'")
            cursor.moveToFirst()
            if (cursor.getInt(0) == 0) {
                db!!.queryData("UPDATE So_om SET Om_Xi3 = 18, Om_Xi4 = 15 WHERE So = '05'")
            }
        } catch (ignored: SQLException) {
        }
    }

    companion object {
        @JvmField
        var Imei: String? = null
        var serial: String? = null
    }
}