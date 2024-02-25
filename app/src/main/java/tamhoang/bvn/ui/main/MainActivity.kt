package tamhoang.bvn.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.drinkless.td.libcore.telegram.TdApi
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tamhoang.bvn.R
import tamhoang.bvn.data.BaseStore
import tamhoang.bvn.data.BusEvent.SetupErrorBagde
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.remote.ApiClient
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.databinding.ActivityMainBinding
import tamhoang.bvn.databinding.CustomactionbarBinding
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.telegram.TelegramClient
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.baoMat.FragChangePassword
import tamhoang.bvn.ui.baocao.FragBaoCaoCu
import tamhoang.bvn.ui.baocao.FragBaoCaoMoi
import tamhoang.bvn.ui.base.nav.NavItem
import tamhoang.bvn.ui.base.nav.NavListAdapter
import tamhoang.bvn.ui.caiDat.FragSetting
import tamhoang.bvn.ui.canBang.FragCanBang
import tamhoang.bvn.ui.chat.FragChatManager
import tamhoang.bvn.ui.chayTrang.Tab_ChayTrang
import tamhoang.bvn.ui.coSoDuLieu.FragDatabase
import tamhoang.bvn.ui.congNo.FragCongno
import tamhoang.bvn.ui.khachHang.FragKhachHang
import tamhoang.bvn.ui.main.fragment.FragCanChuyen
import tamhoang.bvn.ui.main.fragment.FragHome
import tamhoang.bvn.ui.mauTinNhan.FragSmsTemplates
import tamhoang.bvn.ui.menu.ActivityChuyenThang
import tamhoang.bvn.ui.menu.ActivityGiuSo
import tamhoang.bvn.ui.menu.ActivityThaythe
import tamhoang.bvn.ui.tinNhan.TabTinNhan
import tamhoang.bvn.ui.trucTiep.FragTructiepXoso
import tamhoang.bvn.util.ZBroadcast
import tamhoang.bvn.util.extensions.to2ChuSo
import java.util.*

@Keep
class MainActivity : AppCompatActivity(), TelegramClient.Callback {

    private var _bind: ActivityMainBinding? = null
    private val bind get() = _bind!!

    private var _bindBar: CustomactionbarBinding? = null
    private val bindBar get() = _bindBar!!

    lateinit var controller: MainController

    private var currentMenuPosition = -1
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    var db: DbOpenHelper? = null
    private var listNavItems = ArrayList<NavItem>()
    private var notificationNavigated = false
    private var mErrItemCount = 0
    private var onDateSetListener: OnDateSetListener? = null

    @SuppressLint("SetTextI18n", "WrongConstant")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityMainBinding.inflate(layoutInflater)
        _bindBar = CustomactionbarBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)
        ApiClient.init(stringFromJNI())
        controller = MainController(db!!)
        BaseStore.init(this)

        initDate()
//        controller.suaGia()
        controller.loadMappingListToOneJson(assets)
        controller.loadMappingOneByOneJson(assets)

        MainState.refreshDsKhachHang()

        initDrawerMenu()
        initDrawerLayout()

        checkNotificationPermission()
        (getSystemService("notification") as NotificationManager).cancel(1)
        startService(Intent(this, ZBroadcast::class.java))
        toggleNotificationReader()
        MainState.telegramHandle = TelegramHandle(this)
        initNotificationBadge()
    }

    @SuppressLint("SetTextI18n")
    private fun initDate() {
        val calendar = Calendar.getInstance()
        MainState.year = calendar[Calendar.YEAR]
        MainState.month = calendar[Calendar.MONTH] + 1
        MainState.day = calendar[Calendar.DAY_OF_MONTH]
        bindBar.tvDate.text = MainState.day.to2ChuSo() + "-" + MainState.month.to2ChuSo() + "-" + MainState.year
        bindBar.tvDate.setOnClickListener {
            showDialog(0)
        }
        onDateSetListener = OnDateSetListener { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            MainState.year = year
            MainState.month = monthOfYear + 1
            MainState.day = dayOfMonth
            TelegramHandle.sms = true
            bindBar.tvDate.text = MainState.day.to2ChuSo() + "-" + MainState.month.to2ChuSo() + "-" + MainState.year
        }
    }

    private fun initNotificationBadge() {
        bindBar.layoutErrorBadge.setOnClickListener {
            if (!notificationNavigated || currentMenuPosition == -1) {
                notificationNavigated = true
                Toast.makeText(this, "Sang màn sửa tin...", Toast.LENGTH_SHORT).show()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, listFragments[1]).commit()
                title = listNavItems[1].title
                bind.navList.setItemChecked(1, true)
                bind.drawerLayout.closeDrawer(bind.drawerPane)
                return@setOnClickListener
            }
            notificationNavigated = false
            supportFragmentManager.beginTransaction().replace(R.id.main_content, listFragments[currentMenuPosition])
                .commit()
            title = listNavItems[currentMenuPosition].title
            bind.navList.setItemChecked(currentMenuPosition, true)
            bind.drawerLayout.closeDrawer(bind.drawerPane)
        }
        postDelayBadge(SetupErrorBagde(3000))
        setupBadge()
        if (MainState.truncate_mode) {
            bindBar.tvMenu.visibility = View.GONE
            bindBar.layoutErrorBadge.visibility = View.GONE
        }
    }

    private fun initDrawerMenu() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.customView = bindBar.root
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))

        if (!MainState.truncate_mode) {
            listNavItems = arrayListOf(
                NavItem("Trang chủ", "Imei, hạn sử dụng", R.drawable.home),
                NavItem("Sửa tin nhắn", "Sửa/tải lại tin nhắn", R.drawable.edit),
                NavItem("Quản lý tin nhắn", "SMS, Zalo, Viber, WhatsApp", R.drawable.number_report),
                NavItem("Chuyển số/Giữ số", "Chuyển số và giữ số", R.drawable.number_report),
                NavItem("Báo cáo thắng thua", "Báo cáo kết quả từng khách", R.drawable.number_report),
                NavItem("Chạy trang", "Vào trang One789", R.drawable.ld789),
                NavItem("Cân bảng", "Cân bảng trực tiếp", R.drawable.livestream),
                NavItem("Xổ số trực tiếp", "Quay và tính tiền trực tiếp", R.drawable.livekq),
                NavItem("Quản lý công nợ", "Công nợ/Thanh toán", R.drawable.money_report),
                NavItem("Danh sách khách hàng", "Thông tin khách hàng", R.drawable.contact),
                NavItem("Cài đặt", "Cài đặt cho ứng dụng", R.drawable.settings),
                NavItem("Các tin nhắn mẫu", "Các cú pháp chuẩn", R.drawable.guilde),
                NavItem("Bảo mật", "Quản lý mật khẩu/Kích hoạt PM", R.drawable.password),
                NavItem("Kết quả xổ số", "Cập nhật KQ/Tính tiền", R.drawable.database)
            )
            listFragments = arrayListOf(
                FragHome(),
                TabTinNhan(),
                FragChatManager(),
                FragCanChuyen(),
                if (Setting.I.kieuBaoCao.value() == 0) FragBaoCaoCu() else FragBaoCaoMoi(),
                Tab_ChayTrang(),
                FragCanBang(),
                FragTructiepXoso(),
                FragCongno(),
                FragKhachHang(),
                FragSetting(),
                FragSmsTemplates(),
                FragChangePassword(),
                FragDatabase()
            )
        } else {
            listNavItems = arrayListOf(
                NavItem("Trang chủ", "Kết quả Xổ số theo ngày", R.drawable.home),
                NavItem("Xổ số trực tiếp", "Xem Xổ số trực tiếp", R.drawable.livekq),
                NavItem("Thông tin", "Thông tin phần mềm", R.drawable.database)
            )
            listFragments = arrayListOf(
                FragDatabase(),
                FragTructiepXoso(),
                FragHome()
            )
        }
        bind.navList.adapter = NavListAdapter(applicationContext, R.layout.item_nav_list, listNavItems)
        supportFragmentManager.beginTransaction().replace(R.id.main_content, listFragments[0]).commit()

        title = listNavItems[0].title
        bind.navList.setItemChecked(0, true)
        bind.navList.setOnItemClickListener { _, _, position: Int, _ ->
            currentMenuPosition = position
            notificationNavigated = false
            supportFragmentManager.beginTransaction().replace(R.id.main_content, listFragments[position]).commit()
            title = listNavItems[position].title
            bind.navList.setItemChecked(position, true)
            bind.drawerLayout.closeDrawer(bind.drawerPane)
        }
    }

    private fun initDrawerLayout() {
        bind.drawerLayout.closeDrawer(bind.drawerPane)

        actionBarDrawerToggle =
            object : ActionBarDrawerToggle(this, bind.drawerLayout, R.string.drawer_opened, R.string.drawer_closed) {
                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                    @SuppressLint("WrongConstant") val imm = getSystemService("input_method") as InputMethodManager
                    var view = currentFocus
                    if (view == null) {
                        view = View(this@MainActivity)
                    }
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }

                @SuppressLint("WrongConstant")
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                    val imm = getSystemService("input_method") as InputMethodManager
                    var view = currentFocus
                    if (view == null) {
                        view = View(this@MainActivity)
                    }
                    imm.hideSoftInputFromWindow(view!!.windowToken, 0)
                }
            }
        bind.drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()
    }

    private fun toggleNotificationReader() {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, NotificationNewReader::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun postDelayBadge(event: SetupErrorBagde) {
        val handler = Handler()
        handler.postDelayed({
            try {
                val query =
                    "select * from tbl_tinnhanS WHERE phat_hien_loi <> 'ok' AND ngay_nhan = '${MainState.dateYMD}'"
                val cursor = db!!.getData(query)
                mErrItemCount = cursor.count
                cursor.close()
            } catch (e: Exception) {
                mErrItemCount = 0
            }
            setupBadge()
        }, event.delayTime.toLong())
    }

    private fun setupBadge() {
        if (mErrItemCount != 0) {
            bindBar.tvErrorBadge.text = mErrItemCount.toString()
            bindBar.tvErrorBadge.visibility = View.VISIBLE
        } else bindBar.tvErrorBadge.visibility = View.GONE
    }

    private fun checkNotificationPermission() {
        val cn = ComponentName(this, NotificationNewReader::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val enabled = flat != null && flat.contains(cn.flattenToString())
        if (!enabled) {
            AlertDialog.Builder(this)
                .setTitle("Truy cập thông báo!")
                .setMessage("Hãy cho phép phần mềm được truy cập thông báo của điện thoại để kích hoạt chức năng nhắn tin.")
                .setPositiveButton("Ok") { _, _ -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
                .setCanceledOnTouchOutside(false)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        try {
            controller.deleteDir(applicationContext.cacheDir)
        } catch (e: Exception) {
        }
    }

    fun onMenu(v: View?) {
        val menus = arrayOf(
            "Từ điển cá nhân",
            "Nhập dàn giữ số",
            "Cài đặt chuyển thẳng",
            if (TelegramHandle.my_id !== "") "Logout Telegram" else "Login Telegram"
        )
        val popupMenu = PopupMenu(this, v)
        for (i in menus.indices) {
            popupMenu.menu.add(1, i, i, menus[i])
        }
        AlertDialog.Builder(this)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            currentMenuPosition = -1
            notificationNavigated = false
            when (item.order) {
                0 -> startActivity(Intent(this, ActivityThaythe::class.java))
                1 -> startActivity(Intent(this, ActivityGiuSo::class.java))
                2 -> startActivity(Intent(this, ActivityChuyenThang::class.java))
                3 -> {
                    if (TelegramHandle.my_id !== "") {
                        AlertDialog.Builder(this).setTitle("Thoát Telegram?")
                            .setPositiveButton("OK") { _, _ ->
                                MainState.telegramHandle!!.logout(this, db)
                                Toast.makeText(this, "Đã thoát Telegram", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                            .create().show()
                    } else {
                        MainState.telegramHandle!!.showInputPhoneDialog(this, this)
                    }
                }
            }
            true
        }
        popupMenu.show()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).setMessage("Bạn có muốn thoát không?").setCancelable(true)
            .setPositiveButton("Thoát") { _, _ -> exitApp() }
            .setNegativeButton("Không", null).show()
    }

    private fun exitApp() {
        val homeIntent = Intent("android.intent.action.MAIN")
        homeIntent.addCategory("android.intent.category.HOME")
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
    }

    public override fun onCreateDialog(id: Int): Dialog? {
        return if (id != 0) {
            null
        } else DatePickerDialog(
            this,
            onDateSetListener,
            MainState.year,
            MainState.month - 1,
            MainState.day
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle!!.syncState()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != 1) {
            if (requestCode != 2) return
        } else if (grantResults.isEmpty() || grantResults[0] != 0) {
            Toast.makeText(applicationContext, "Can't access messages.", Toast.LENGTH_SHORT).show()
            return
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != 0
            && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 2)
        }
        if (grantResults.isEmpty() || grantResults[0] != 0) {
            Toast.makeText(applicationContext, "Can't access messages.", Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResult(`object`: TdApi.Object) {
        MainState.telegramHandle!!.onResult(`object`, db, applicationContext, this, this)
    }

    private external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("dev")
        }

        var listFragments = mutableListOf<Fragment>()

        fun refreshFragBaoCao() {
            listFragments[4] = if (Setting.I.kieuBaoCao.value() == 0) FragBaoCaoCu() else FragBaoCaoMoi()
        }
    }

}