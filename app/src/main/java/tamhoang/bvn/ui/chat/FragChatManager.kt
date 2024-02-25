package tamhoang.bvn.ui.chat

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.DatabaseUtils.InsertHelper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.BaseStore.selectChats
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.Chat
import tamhoang.bvn.databinding.FragChatManagerBinding
import tamhoang.bvn.databinding.FragChatManagerLvBinding
import tamhoang.bvn.messageCenter.notification.NotificationNewReader
import tamhoang.bvn.messageCenter.telegram.TelegramHandle
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.khachHang.ActivityAddKH
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.contains
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FragChatManager : Fragment() {
    private var _bind: FragChatManagerBinding? = null
    private val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var handler: Handler? = null
    private val listChat: MutableList<Chat> = ArrayList()
    private val runnable = object : Runnable {
        override fun run() {
            if (TelegramHandle.sms) {
                xemListview()
                TelegramHandle.sms = false
            }
            handler!!.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragChatManagerBinding.inflate(inflater)
        db = DbOpenHelper(activity!!)
        bind.btnThongbao.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        bind.listviewKH.onItemClickListener = OnItemClickListener { _, _, i: Int, _ ->
            if (listChat.size <= i) return@OnItemClickListener
            val (_, _, _, _, ten_kh, so_dienthoai, use_app) = listChat[i]
            val intent = Intent(activity, Chatbox::class.java)
            intent.putExtra("tenKH", ten_kh)
            intent.putExtra("so_dienthoai", so_dienthoai)
            intent.putExtra("app", use_app)
            startActivity(intent)
        }
        notificationPermission()
        val handler2 = Handler()
        handler = handler2
        handler2.postDelayed(runnable, 1000)
        return bind.root
    }

    override fun onDestroy() {
        super.onDestroy()
        handler!!.removeCallbacks(runnable)
    }

    override fun onResume() {
        getSMS()
        xemListview()
        super.onResume()
    }

    // thay the data sms trong Chat_database = content://sms
    private fun getSMS() {
        val database: SQLiteDatabase
        val mDate = MainState.dateYMD
        if (ContextCompat.checkSelfPermission(activity!!, "android.permission.READ_SMS") == 0) {
            try {
                val dateStart = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(mDate + "T00:00:00")
                val filter = "date>=" + dateStart.time
                db!!.queryData("DELETE FROM Chat_database WHERE ngay_nhan = '$mDate' AND use_app = 'sms'")
                val cur = db!!.getData("Select * From tbl_kh_new")
                val json = JSONObject()
                while (cur.moveToNext()) {
                    try {
                        val jsonKh = JSONObject().apply {
                            put("type_kh", cur.getString(3))
                            put("ten_kh", cur.getString(0))
                            put("sdt", cur.getString(1))
                            put("so_tn", 0)
                        }
                        json.put(cur.getString(1), jsonKh)
                    } catch (e: SQLiteException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                cur.close()
                val message = Uri.parse("content://sms")
                val cr = activity!!.contentResolver
                val c = cr.query(message, null, filter, null, "date ASC")
                activity!!.startManagingCursor(c)
                val totalSMS = c!!.count
                if (c.moveToFirst()) {
                    database = db!!.writableDatabase
                    val ih = InsertHelper(database, "Chat_database")
                    try {
                        database.beginTransaction()
                        var i = 0
                        while (i < totalSMS) {
                            val millis = c.getLong(c.getColumnIndexOrThrow("date"))
                            val mGioNhan = (DateFormat.format("HH:mm:ss", Date(millis)) as Any).toString()
                            var mSDT = c.getString(c.getColumnIndexOrThrow("address")).replace(" ", "")
                            val body = c.getString(c.getColumnIndexOrThrow("body")).replace("'", " ")
                                .replace("\"", " ")
                            val typeKT = c.getString(c.getColumnIndexOrThrow("type"))
                            if (mSDT.startsWith("0")) mSDT = "+84" + mSDT.substring(1)
                            db!!.queryData("Update tbl_tinnhanS set gio_nhan ='$mGioNhan' WHERE nd_goc = '$body' AND so_dienthoai = '$mSDT' AND ngay_nhan = '$mDate'")
                            if (json.has(mSDT)) {
                                val giaKhach = json.getJSONObject(mSDT)
                                giaKhach.put("so_tn", giaKhach.getInt("so_tn") + 1)
                                giaKhach.put(body, body)
                                ih.prepareForInsert()
                                ih.bind(ih.getColumnIndex("ngay_nhan"), mDate)
                                ih.bind(ih.getColumnIndex("gio_nhan"), mGioNhan)
                                ih.bind(ih.getColumnIndex("type_kh"), typeKT)
                                ih.bind(ih.getColumnIndex("ten_kh"), giaKhach.getString("ten_kh"))
                                ih.bind(ih.getColumnIndex("so_dienthoai"), mSDT)
                                ih.bind(ih.getColumnIndex("use_app"), "sms")
                                ih.bind(ih.getColumnIndex("nd_goc"), body)
                                ih.bind(ih.getColumnIndex("del_sms"), 1)
                                ih.execute()
                                json.put(mSDT, giaKhach)
                            }
                            c.moveToNext()
                            i++
                        }
                        database.setTransactionSuccessful()
                        database.endTransaction()
                        ih.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        database.endTransaction()
                        ih.close()
                        database.close()
                    }
                    database.close()
                }
            } catch (e: SQLiteException) {
                e.printStackTrace()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
    }

    private fun xemListview() { //lay data trong Chat_database (chi lay moi khach hang 1 row) hien thi ra listviewKH
        listChat.clear()
        val mDate = MainState.dateYMD
        val listChatAll = selectChats(mDate)
        val setTenKh: MutableSet<String> = HashSet()
        for (chat in listChatAll) {
            val tenKh = chat.ten_kh
            val useApp = chat.use_app
            if ((MainState.contactsMap.containsKey(tenKh) || useApp.contains(listOf("sms", "ZL", "TL", "VB")))
                && !setTenKh.contains(tenKh)
            ) {
                setTenKh.add(tenKh)
                listChat.add(chat)
            }
        }
        for (tenKh in MainState.contactsMap.keys) {
            if (!setTenKh.contains(tenKh)) {
                var useApp = ""
                if (tenKh.contains("ZL")) {
                    useApp = "ZL"
                } else if (tenKh.contains("VB")) {
                    useApp = "VB"
                } else if (tenKh.contains("WA")) {
                    useApp = "WA"
                }
                listChat.add(
                    Chat(0, "", "", 2, tenKh, tenKh, useApp, "Hôm nay chưa có tin nhắn!", 0)
                )
            }
        }
        if (activity != null) {
            val listTenKH = listChat.map { it.ten_kh }
            bind.listviewKH.adapter = ChatMain(activity, R.layout.frag_chat_manager_lv, listTenKH)
        }
    }

    private fun notificationPermission() {
        val enabled: Boolean
        val cn = ComponentName(activity!!, NotificationNewReader::class.java)
        val flat = Settings.Secure.getString(activity!!.contentResolver, "enabled_notification_listeners")
        enabled = flat != null && flat.contains(cn.flattenToString())
        if (!enabled) {
            Dialog.simple(
                activity!!, "Truy cập thông báo!",
                "Hãy cho phép phần mềm được truy cập thông báo của điện thoại để kích hoạt chức năng nhắn tin.",
                positiveText = "Ok",
                positiveAction = {
                    activity!!.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                },
                negativeText = "Cancel",
                cancelable = false
            )
        }
    }

    inner class ChatMain(context: Context?, resource: Int, objects: List<String>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        internal inner class ViewHolder(
            val tvTenKH: TextView,
            val addContacts: ImageButton,
            var imageView: ImageView,
            var ndChat: TextView,
            var tvDelete: TextView
        )

        @SuppressLint("WrongConstant")
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val (_, _, _, _, ten_kh, so_dienthoai, use_app, nd_goc) = listChat[position]
            val bind = FragChatManagerLvBinding.inflate(layoutInflater)
            val holder = ViewHolder(bind.tvKhachHang, bind.addContacts, bind.imvApp, bind.tvNoiDung, bind.tvDelete)
            if (use_app.contains("WA")) {
                holder.imageView.setBackgroundResource(R.drawable.ic_perm_phone_msg)
            } else if (use_app.contains("VI")) {
                holder.imageView.setBackgroundResource(R.drawable.ic_phone)
            } else if (use_app.contains("ZL")) {
                holder.imageView.setBackgroundResource(R.drawable.ic_zalo)
            } else if (use_app.contains("TL")) {
                holder.imageView.setBackgroundResource(R.drawable.outline_telegram_20)
                holder.tvDelete.visibility = View.GONE
            } else if (use_app.contains("sms")) {
                holder.imageView.setBackgroundResource(R.drawable.ic_sms)
                holder.addContacts.visibility = View.GONE
                holder.tvDelete.visibility = View.GONE
            }
            holder.addContacts.isFocusable = false
            holder.addContacts.isFocusableInTouchMode = false
            holder.addContacts.setOnClickListener {
                val intent = Intent(activity, ActivityAddKH::class.java)
                intent.putExtra("tenKH", ten_kh)
                intent.putExtra("so_dienthoai", so_dienthoai)
                intent.putExtra("use_app", use_app)
                startActivity(intent)
            }
            if (MainState.DSkhachhang.contains(so_dienthoai)) {
                holder.addContacts.visibility = View.GONE
            }
            holder.tvDelete.setOnClickListener {
                Dialog.simple(
                    activity!!,
                    "Xoá Khách",
                    "Sẽ xóa hết dữ liệu chat từ khách này, không thể khôi phục và không thể tải lại tin nhắn!",
                    positiveText = "Có",
                    positiveAction = {
                        val contact = MainState.contactsMap[ten_kh]
                        if (contact != null) {
                            MainState.contactsMap.remove(ten_kh)
                        }
                        xemListview()
                        Toast.makeText(activity, "Đã xóa!", 1).show()
                    },
                    negativeText = "Không"
                )
            }
            holder.tvTenKH.text = ten_kh
            holder.ndChat.text = nd_goc
            return bind.root
        }
    }
}