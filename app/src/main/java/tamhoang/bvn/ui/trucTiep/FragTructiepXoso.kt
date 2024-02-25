package tamhoang.bvn.ui.trucTiep

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.internal.view.SupportMenu
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.FragCanchuyenLvBinding
import tamhoang.bvn.databinding.TructiepxosoBinding
import tamhoang.bvn.messageCenter.notification.NotificationBindObject
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.toDecimal
import tamhoang.bvn.util.ld.CongThuc.checkTime
import java.io.IOException
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

class FragTructiepXoso : Fragment() {
    private var _bind: TructiepxosoBinding? = null
    val bind get() = _bind!!

    var dangXuat = "lo"
    var soGiai = 0
    private var url = Const.URL_XOSOME_KQMB
    var db: DbOpenHelper? = null
    var handler: Handler? = null
    var jsonValues = ArrayList<JSONObject>()
    var listSo = ArrayList<String>()
    var mDate = ""
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (listSo.size > 26) {
                handler!!.removeCallbacks(this)
                return
            }
            if (bind.rbThienPhu.isChecked) {
                loadJavascript("(function() { return document.getElementsByClassName('table table-lotto-xsmb')[0].innerText;; })();")
            } else {
                loadJavascript("(function() { return document.getElementsByClassName('firstlast-mb fl')[0].innerText;; })();")
            }
            handler!!.postDelayed(this, 2000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        _bind = TructiepxosoBinding.inflate(inflater)
        db = DbOpenHelper(activity!!)
        Calendar.getInstance().time = Date()
        SimpleDateFormat("yyyy-MM-dd").timeZone = TimeZone.getDefault()
        bind.switchTructiep.setOnCheckedChangeListener { _, isChecked ->
            bind.webview.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        bind.rbXemLo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dangXuat = "lo"
                xemLv()
            }
        }
        bind.rbXemXien.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dangXuat = "xi"
                xemLv()
            }
        }
        bind.rbXsoMe.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                url = Const.URL_XOSOME_KQMB
                bind.webview.loadUrl(url)
            }
        }
        bind.rbThienPhu.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val mDate = MainState.date2DMY.replace("/", "-")
                url = "https://xosothienphu.com/ma-nhung/xsmb-$mDate.html"
                bind.webview.loadUrl(url)
            }
        }
        handler = Handler()
        if (!checkTime("18:14") || checkTime("24:30")) {
            bind.webview.visibility = View.GONE
        } else {
            bind.switchTructiep.text = "Ẩn/hiện bảng Kết quả"
            handler!!.postDelayed(runnable, 3000)
            bind.webview.visibility = View.GONE
        }
        bind.webview.addJavascriptInterface(NotificationBindObject(activity!!.applicationContext), "NotificationBind")
        setUpWebViewDefaults(bind.webview)
        if (savedInstanceState != null) {
            bind.webview.restoreState(savedInstanceState)
        }
        if (bind.webview.url == null) {
            bind.webview.loadUrl(Const.URL_XOSOME_KQMB)
        }
        bind.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                loadJavascript("document.getElementsByClassName('embeded-breadcrumb')[0].style.display = 'none';\ndocument.getElementsByClassName('tit-mien')[0].style.display = 'none';")
                bind.webview.visibility = View.VISIBLE
                bind.switchTructiep.isEnabled = true
            }
        }
        bind.webview.isEnabled = false
        xemLv()
        return bind.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebViewDefaults(webView: WebView?) {
        val settings = webView!!.settings
        settings.javaScriptEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        WebView.setWebContentsDebuggingEnabled(true)
    }

    fun loadJavascript(javascript: String?) {
        bind.webview.evaluateJavascript(javascript!!) { s: String ->
            var msg = ""
            val reader = JsonReader(StringReader(s))
            reader.isLenient = true
            try {
                if (reader.peek() != JsonToken.NULL && reader.peek() == JsonToken.STRING && reader.nextString()
                        .also { msg = it } != null && msg.contains("\n")
                ) {
                    val SSS =
                        msg.substring(msg.indexOf("0")).split("\n").map { if (it.length > 2) it.substring(2) else "" }
                    SSS.map { }
                    if (SSS.size == 10) {
                        listSo = ArrayList()
                        for (i2 in SSS.indices) {
                            val Sodit = SSS[i2].replace(" ".toRegex(), "").split(",").toTypedArray()
                            for (value in Sodit) {
                                if (value.length == 1) {
                                    listSo.add(i2.toString() + value)
                                } else if (value.length == 2) {
                                    listSo.add(i2.toString() + value.substring(1))
                                }
                            }
                        }
                        if (listSo.size != soGiai) {
                            tinhTienTuDong(listSo)
                            xemLv()
                            soGiai = listSo.size
                        }
                    } else {
                        Toast.makeText(activity, "Trang xoso.me đang bị lỗi!", Toast.LENGTH_LONG).show()
                        handler!!.removeCallbacks(runnable)
                    }
                }
                reader.close()
            } catch (e2: IOException) {
                Log.e(ContentValues.TAG, "MainActivity: IOException", e2)
            } catch (ignored: Throwable) {
            }
        }
    }

    override fun onStop() {
        super.onStop()
        bind.webview.clearCache(true)
        handler!!.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        if (checkTime("18:14") && !checkTime("18:30") && isNetworkConnected) {
            bind.switchTructiep.text = "Ẩn/hiện bảng Kết quả"
            handler!!.postDelayed(runnable, 3000)
        }
    }

    private val isNetworkConnected: Boolean
        get() {
            @SuppressLint("WrongConstant") val activeNetworkInfo =
                (activity!!.getSystemService("connectivity") as ConnectivityManager).activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    private fun tinhTienTuDong(ArraySo: ArrayList<String>) {
        db!!.queryData("Update tbl_soctS Set so_nhay = 0, ket_qua = 0 WHERE ngay_nhan = '$mDate' AND the_loai <> 'tt' AND the_loai <> 'cn'")
        var ketqua = ""
        for (so in ArraySo) {
            db!!.queryData("Update tbl_soctS Set so_nhay = so_nhay + 1 Where the_loai = 'lo' and ngay_nhan = '$mDate' And so_chon ='$so'")
            ketqua = "$ketqua$so,"
        }
        val cursor = db!!.getData("Select * From tbl_soctS Where ngay_nhan = '$mDate' AND the_loai = 'xi'")
        while (cursor.moveToNext()) {
            val soChons = cursor.getString(7).split(",")
            val check = soChons.all { s -> ketqua.contains(s) }
            if (check) {
                db!!.queryData("Update tbl_soctS Set so_nhay = 1 WHERE ID = " + cursor.getString(0))
            }
        }
        db!!.queryData("Update tbl_soctS set ket_qua = diem * lan_an * so_nhay - tong_tien WHERE ngay_nhan = '$mDate' AND type_kh = 1 AND the_loai <> 'tt' AND the_loai <> 'cn'")
        db!!.queryData("Update tbl_soctS set ket_qua = -diem * lan_an * so_nhay + tong_tien WHERE ngay_nhan = '$mDate' AND type_kh = 2 AND the_loai <> 'tt' AND the_loai <> 'cn'")
    }

    private fun xemLv() {
        if (MainState.truncate_mode) return
        jsonValues = ArrayList()
        mDate = MainState.dateYMD
        val str = if (dangXuat === "lo") {
            "Select tbl_soctS.So_chon\n, Sum((tbl_soctS.type_kh = 1) * (100-tbl_soctS.diem_khachgiu)*diem_quydoi/100) as diem\n, 0, Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as chuyen\n, Sum((tbl_soctS.type_kh =1) * (100-tbl_soctS.diem_khachgiu-tbl_soctS.diem_dly_giu)*diem_quydoi/100) - Sum((tbl_soctS.type_kh =2) * tbl_soctS.diem_quydoi) as ton\n, so_nhay  From so_om Left Join tbl_soctS On tbl_soctS.so_chon = so_om.So \n Where tbl_soctS.ngay_nhan='$mDate' AND tbl_soctS.the_loai='lo' \n GROUP by so_om.So Order by ton DESC, diem DESC"
        } else {
            db!!.getData("Select * From So_om WHERE ID = 1").moveToFirst()
            "SELECT so_chon, sum((type_kh =1)*(100-diem_khachgiu)*diem)/100 AS diem, 0, SUm((type_kh =2)*diem) as chuyen , SUm((type_kh =1)*(100-diem_khachgiu-diem_dly_giu)*diem/100)-SUm((type_kh =2)*diem) AS ton, so_nhay   From tbl_soctS Where ngay_nhan='$mDate' AND the_loai='xi' GROUP by so_chon Order by ton DESC, diem DESC"
        }
        val cursor = db!!.getData(str)
        while (cursor.moveToNext()) {
            try {
                val jSonSo = JSONObject()
                if (dangXuat === "lo") {
                    jSonSo.put("so_chon", cursor.getString(0))
                    jSonSo.put("xep_diem", cursor.getInt(5))
                } else if (listSo.size > 0) { //xien
                    val soXiens = cursor.getString(0).split(",")
                    val soXienStr = soXiens.joinToString("") {
                        if (listSo.contains(it)) "<font color='#FF0000'>$it</font>," else "$it,"
                    }
                    val demXienAn = soXiens.count { listSo.contains(it) }
                    val xepDiem = when (soXiens.size) {
                        2 -> when (demXienAn) {
                            2 -> 800
                            1 -> 80
                            else -> 0
                        }
                        3 -> when (demXienAn) {
                            3 -> 900
                            2 -> 90
                            1 -> 60
                            else -> 0
                        }
                        4 -> when (demXienAn) {
                            4 -> 1000
                            3 -> 100
                            2 -> 70
                            1 -> 50
                            else -> 0
                        }
                        else -> 0
                    }
                    jSonSo.put("so_chon", soXienStr)
                    jSonSo.put("xep_diem", xepDiem)
                } else {
                    jSonSo.put("so_chon", cursor.getString(0))
                    jSonSo.put("xep_diem", 0)
                }
                jSonSo.put("tien_nhan", cursor.getInt(1).toDecimal())
                jSonSo.put("tien_om", cursor.getInt(2).toDecimal())
                jSonSo.put("tien_chuyen", cursor.getInt(3).toDecimal())
                jSonSo.put("tien_ton", cursor.getInt(4).toDecimal())
                jSonSo.put("so_nhay", cursor.getInt(5))
                if (dangXuat != "lo" && cursor.getInt(4) <= 0) {
                    break
                }
                jsonValues.add(jSonSo)
            } catch (e: JSONException) {
                e.printStackTrace()
                cursor.close()
            }
        }
        jsonValues.sortWith(compareByDescending { it.getInt("xep_diem") })

        if (!cursor.isClosed) cursor.close()

        if (activity != null) {
            bind.listview.adapter = SoOmAdapter(activity, R.layout.frag_canchuyen_lv, jsonValues)
        }
    }

    inner class SoOmAdapter(context: Context?, resource: Int, objects: List<JSONObject?>?) : ArrayAdapter<Any?>(
        context!!, resource, objects!!
    ) {
        internal inner class ViewHolder(val bind: FragCanchuyenLvBinding)

        @SuppressLint("WrongConstant", "RestrictedApi", "SetTextI18n")
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            var view = mView
            val holder: ViewHolder
            if (mView == null) {
                val bind = FragCanchuyenLvBinding.inflate(layoutInflater)
                holder = ViewHolder(bind)
                view = bind.root
                view.tag = holder
            } else {
                holder = mView.tag as ViewHolder
            }
            val json = jsonValues[position]
            try {
                if (json.getInt("so_nhay") > 0) {
                    holder.bind.TvSo.setTextColor(SupportMenu.CATEGORY_MASK)
                    holder.bind.tvDiemNhan.setTextColor(SupportMenu.CATEGORY_MASK)
                    holder.bind.tvDiemOm.setTextColor(SupportMenu.CATEGORY_MASK)
                    holder.bind.tvDiemChuyen.setTextColor(SupportMenu.CATEGORY_MASK)
                    holder.bind.tvDiemTon.setTextColor(SupportMenu.CATEGORY_MASK)
                    holder.bind.TvSo.text = Html.fromHtml(json.getString("so_chon")).toString() +
                            "*".repeat(json.getInt("so_nhay"))
                } else {
                    holder.bind.TvSo.setTextColor(SupportMenu.CATEGORY_MASK)
                    holder.bind.tvDiemNhan.setTextColor(View.MEASURED_STATE_MASK)
                    holder.bind.tvDiemOm.setTextColor(View.MEASURED_STATE_MASK)
                    holder.bind.tvDiemChuyen.setTextColor(View.MEASURED_STATE_MASK)
                    holder.bind.tvDiemTon.setTextColor(View.MEASURED_STATE_MASK)
                    holder.bind.TvSo.apply {
                        setTextColor(View.MEASURED_STATE_MASK)
                        text = Html.fromHtml(json.getString("so_chon"))
                    }
                }
                holder.bind.tvDiemNhan.text = json.getString("tien_nhan")
                holder.bind.tvDiemOm.text = json.getString("tien_om")
                holder.bind.tvDiemChuyen.text = json.getString("tien_chuyen")
                holder.bind.tvDiemTon.text = json.getString("tien_ton")
                holder.bind.stt.text = (position + 1).toString() + ""
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return holder.bind.root
        }
    }

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION"
    }
}