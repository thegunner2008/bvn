package tamhoang.bvn.ui.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.BaseStore.getMaxSoTinNhan
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.TinNhanS
import tamhoang.bvn.data.services.guiTinNhan.GuiTinNhanService
import tamhoang.bvn.data.services.xuLyTinNhan.XuLyTinNhanService
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.data.store.TinNhanStore
import tamhoang.bvn.databinding.ActivityChatboxBinding
import tamhoang.bvn.databinding.MessageListItemInBinding
import tamhoang.bvn.databinding.MessageListItemOutBinding
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.ld.CongThuc.checkDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Chatbox : BaseToolBarActivity() {
    private var _bind: ActivityChatboxBinding? = null
    private val bind get() = _bind!!

    var tinXuly = JSONObject()
    var appUse: String? = null
    var db: DbOpenHelper? = null
    private val gioNhan = ArrayList<String>()
    var handler: Handler? = null
    private val mApp = ArrayList<String>()
    private val mID = ArrayList<String>()
    private val idTinNhans = ArrayList<String>()
    private val sdts = ArrayList<String>()
    private val soTinnhans = ArrayList<String>()
    private val mTenKH = ArrayList<String>()
    private val mXulytin = ArrayList<String>()
    private val ndGocs = ArrayList<String>()
    var position = 0
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                xemLv()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }
    var soDienthoai: String? = null
    var tenKh: String? = null
    private val typeKh = ArrayList<String>()
    override fun getLayoutId(): Int {
        return R.layout.activity_chatbox
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityChatboxBinding.inflate(layoutInflater)
        setContentView(bind.root)
        tinXuly = JSONObject()
        db = DbOpenHelper(this)
        val intent = intent
        tenKh = intent.getStringExtra("tenKH")
        soDienthoai = intent.getStringExtra("so_dienthoai")
        appUse = intent.getStringExtra("app")
        bind.send.setOnClickListener {
            val mess = bind.messageS.text.toString()
            try {
                if (mess.replace(" ", "").isNotEmpty()) {
                    val mNgayNhan = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val mGionhan = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    if (appUse!!.contains("TL")) {
                        try {
                            TelegramHandle.sendMessage(soDienthoai!!.toLong(), mess)
                            guiTinTrucTiep(mNgayNhan, mGionhan, tenKh, mess)
                            bind.messageS.setText("")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (!appUse!!.contains("sms")) {
                        NotificationNewReader().reply(tenKh, mess)
                        db!!.queryData("Insert into Chat_database Values( null,'$mNgayNhan', '$mGionhan', 2, '$tenKh', '$soDienthoai', '$appUse','$mess',1)")
                        bind.messageS.setText("")
                        guiTinTrucTiep(mNgayNhan, mGionhan, tenKh, mess)
                        TelegramHandle.sms = true
                        xemLv()
                    } else {
                        try {
                            val c = db!!.getData("Select * From tbl_kh_new Where ten_kh = '$tenKh'")
                            c.moveToFirst()
                            GuiTinNhanService.I.sendSMS(c.getString(1), mess)
                            db!!.queryData("Insert into Chat_database Values( null,'$mNgayNhan', '$mGionhan', 2, '$tenKh', '$soDienthoai', '$appUse','$mess',1)")
                            bind.messageS.setText("")
                            guiTinTrucTiep(mNgayNhan, mGionhan, tenKh, mess)
                            TelegramHandle.sms = true
                            xemLv()
                            c.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bind.Listview.onItemLongClickListener =
            OnItemLongClickListener { _, _, i: Int, _ ->
                position = i
                false
            }
        handler = Handler()
        handler!!.postDelayed(runnable, 1000)
        registerForContextMenu(bind.Listview)
        xemLv()
    }

    fun guiTinTrucTiep(Ngay_gui: String, Gio_gui: String?, Ten_kh: String?, mText: String) {
        val khachHang = KhachHangStore.I.selectByName(Ten_kh!!)
        if (khachHang != null) {
            if (khachHang.type_kh > 1) {
                val maxSoTn = getMaxSoTinNhan(Ngay_gui, khachHang.type_kh, "ten_kh = '$Ten_kh'")
                val ndTinNhan = mText.replace("'".toRegex(), " ").trim { it <= ' ' }
                val tinNhanS = TinNhanS(
                    null, Ngay_gui, Gio_gui!!, 2, Ten_kh, khachHang.sdt, khachHang.use_app,
                    maxSoTn + 1, ndTinNhan, ndTinNhan, ndTinNhan, "ko", 0, 0, 0, null
                )
                TinNhanStore.I.insert(tinNhanS)
                val tinNhanS2 = TinNhanStore.I.select(Ngay_gui, Ten_kh, maxSoTn + 1, 2)
                if (checkDate(MainState.hanSuDung)) {
                    try {
                        XuLyTinNhanService.I.upsertTinNhan(tinNhanS2!!.ID!!, khachHang.type_kh)
                    } catch (e: Exception) {
                        db!!.queryData("Update tbl_tinnhanS set phat_hien_loi = 'ko' WHERE id = " + tinNhanS2!!.ID)
                        db!!.queryData("Delete From tbl_soctS WHERE ngay_nhan = '" + Ngay_gui + "' AND ten_kh = '" + Ten_kh + "' AND so_tin_nhan = " + (maxSoTn + 1) + " And type_kh = 2")
                        Toast.makeText(this, "Đã xảy ra lỗi!", Toast.LENGTH_LONG).show()
                    } catch (throwable: Throwable) {
                        throwable.printStackTrace()
                    }
                } else if (MainState.tenAcc().isEmpty()) {
                    Dialog.simple(
                        this, "Thông báo",
                        "Kiểm tra kết nối Internet!",
                        negativeText = "Đóng"
                    )
                } else {
                    try {
                        Dialog.simple(
                            this, "Thông báo",
                            "Đã hết hạn sử dụng phần mềm \n\nHãy liên hệ đại lý hoặc SĐT: ${MainState.thongTinAcc.phone} để gia hạn",
                            negativeText = "Đóng"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        handler!!.removeCallbacks(runnable)
    }

    public override fun onResume() {
        xemLv()
        super.onResume()
    }

    fun xemLv() {
        mID.clear()
        mTenKH.clear()
        gioNhan.clear()
        typeKh.clear()
        ndGocs.clear()
        mApp.clear()
        mXulytin.clear()
        idTinNhans.clear()
        soTinnhans.clear()
        val mDate = MainState.dateYMD
        val database = db
        val cursor = database!!.getData(
            """
    Select chat_database.*, tbl_tinnhanS.phat_hien_loi, tbl_tinnhanS.id, tbl_tinnhanS.so_tin_nhan From chat_database 
    LEFT JOIN tbl_tinnhanS ON chat_database.ngay_nhan = tbl_tinnhanS.ngay_nhan AND chat_database.gio_nhan = tbl_tinnhanS.gio_nhan AND chat_database.ten_kh = tbl_tinnhanS.ten_kh AND chat_database.nd_goc = tbl_tinnhanS.nd_goc
    Where chat_database.ten_kh = '${tenKh}'  AND chat_database.ngay_nhan = '$mDate' AND chat_database.del_sms = 1 ORDER by gio_nhan
    """.trimIndent()
        )
        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                mID.add(cursor.getString(0))
                mTenKH.add(cursor.getString(4))
                sdts.add(cursor.getString(5))
                gioNhan.add(cursor.getString(2))
                typeKh.add(cursor.getString(3))
                ndGocs.add(cursor.getString(7))
                mApp.add(cursor.getString(6))
                if (cursor.isNull(9)) {
                    mXulytin.add("")
                    idTinNhans.add("")
                    soTinnhans.add("")
                } else {
                    mXulytin.add(cursor.getString(9))
                    idTinNhans.add(cursor.getString(10))
                    soTinnhans.add(cursor.getString(11))
                }
            }
            cursor.close()
        }
        bind.Listview.adapter = Chat(this, R.layout.message_list_item_in, mTenKH)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val database = db
        val cursor = database!!.getData("Select * From tbl_kh_new Where ten_kh = '$tenKh'")
        if (cursor.count != 0) {
            menu.add(SUA_TIN)
            menu.add(XEM_CHI_TIET)
        }
        menu.add(COPY)
        menu.add(XOA)
        cursor.close()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        @SuppressLint("WrongConstant") val clipboard = getSystemService("clipboard") as ClipboardManager
        when (item.title) {
            SUA_TIN -> {
                if (mXulytin[position].isNotEmpty()) {
                    val intent = Intent(this, ActivityTinnhan::class.java)
                    intent.putExtra("m_ID", idTinNhans[position])
                    startActivity(intent)
                }
            }
            XEM_CHI_TIET -> {
                if (mXulytin[position].indexOf("ok") == 0) {
                    val intent2 = Intent(this, ActivityCTTinnhan::class.java)
                    intent2.putExtra("m_ID", idTinNhans[position])
                    intent2.putExtra("type_kh", typeKh[position])
                    startActivity(intent2)
                }
            }
            COPY -> {
                clipboard.setPrimaryClip(ClipData.newPlainText("Tin nhắn:", ndGocs[position]))
                Toast.makeText(this, "Đã copy vào bộ nhớ tạm!", Toast.LENGTH_LONG).show()
            }
            XOA -> {
                Dialog.simple(
                    this,
                    "Xóa tin này",
                    positiveText = "YES",
                    positiveAction = {
                        if (mXulytin[position].isNotEmpty()) {
                            val tinnhan = TinNhanStore.I.selectByID(idTinNhans[position].toInt()) ?: return@simple
                            db!!.queryData(
                                "DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '${tinnhan.ngayNhan}' AND ten_kh = "
                                        + "'${tinnhan.tenKh}' AND so_tin_nhan = ${tinnhan.soTinNhan} AND type_kh = ${tinnhan.typeKh}"
                            )
                            db!!.queryData(
                                "DELETE FROM tbl_soctS WHERE ngay_nhan = '${tinnhan.ngayNhan}' AND ten_kh = "
                                        + "'${tinnhan.tenKh}' AND so_tin_nhan = ${tinnhan.soTinNhan} AND type_kh = ${tinnhan.typeKh}"
                            )

                            db!!.queryData("Update chat_database set del_sms = 0 WHERE ID = ${mID[position]}")
                            xemLv()
                            Toast.makeText(this@Chatbox, "Đã xóa!", Toast.LENGTH_LONG).show()
                            return@simple
                        }
                        db!!.queryData("Update chat_database set del_sms = 0 WHERE ID = ${mID[position]}")
                        xemLv()
                    },
                    negativeText = "NO"
                )
            }
        }
        return true
    }

    inner class Chat(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        override fun getView(position: Int, mConvertView: View?, parent: ViewGroup): View {
            if (typeKh[position].contains("2")) {
                val bind = MessageListItemOutBinding.inflate(layoutInflater)
                if (mXulytin[position].indexOf("ok") == 0) {
                    val spanString = SpannableString(ndGocs[position])
                    spanString.setSpan(StyleSpan(1), 0, spanString.length, 0)
                    bind.bodyOut.text = spanString
                } else {
                    bind.bodyOut.text = ndGocs[position]
                }
                bind.statusOut.text = gioNhan[position]
                return bind.root
            }
            if (typeKh[position].contains("1")) {
                val bind = MessageListItemInBinding.inflate(layoutInflater)
                if (mXulytin[position].indexOf("ok") == 0) {
                    val spanString = SpannableString(ndGocs[position])
                    spanString.setSpan(StyleSpan(1), 0, spanString.length, 0)
                    bind.bodyIn.text = spanString
                } else {
                    bind.bodyIn.text = ndGocs[position]
                }
                bind.statusIn.text = gioNhan[position]
                return bind.root
            }
            return super.getView(position, mConvertView, parent)
        }
    }

    private val SUA_TIN = "Sửa tin"
    private val XEM_CHI_TIET = "Xem chi tiết"
    private val COPY = "Copy"
    private val XOA = "Xóa"
}