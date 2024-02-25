package tamhoang.bvn.ui.coSoDuLieu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.akaman.AkaManSec
import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.services.TinhTienService
import tamhoang.bvn.databinding.FragDatabaseBinding
import tamhoang.bvn.messageCenter.notification.NotificationBindObject
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.clearDuplicate
import tamhoang.bvn.util.ld.CongThuc.isNumeric
import java.io.IOException
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Function

class FragDatabase : Fragment() {
    private var _bind: FragDatabaseBinding? = null
    private val bind get() = _bind!!

    private lateinit var giais: List<String>
    var db: DbOpenHelper? = null

    @SuppressLint("WrongConstant", "HardwareIds")  // android.support.v4.app.Fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        db = DbOpenHelper(activity!!)
        AkaManSec.queryAkaManPwd(db)
        _bind = FragDatabaseBinding.inflate(inflater)

        bind.nazzy.setOnCheckedChangeListener { _, isChecked ->
            bind.gr1.clearCheck()
            if (!isChecked) return@setOnCheckedChangeListener
            try {
                val dateMDY = MainState.date3MDY
                val date = MainState.dateYMD
                Volley.newRequestQueue(activity).add(object : StringRequest(1,
                    "http://thongke.nazzy.vn/handler/thongke.ashx?t=kqxsmb&date=$dateMDY",
                    Response.Listener { response: String? ->
                        var str = ""
                        try {
                            if (response == null) return@Listener
                            val outerObject = JSONObject(response)
                            if (outerObject.getString("Ngay").contains(MainState.date2DMY)) {
                                db!!.queryData("Delete From ketqua WHERE ngay = '$date'")
                                str = listOf(
                                    "GDB", "G1", "G21", "G22", "G31", "G32", "G33", "G34", "G35", "G36",
                                    "G41", "G42", "G43", "G44", "G51", "G52", "G53", "G54", "G55", "G56",
                                    "G61", "G62", "G63", "G71", "G72", "G73", "G74"
                                )
                                    .joinToString(",") { "'${outerObject.getString(it)}'" }
                            }
                            if (str.isNotEmpty()) {
                                db!!.queryData("Insert Into ketqua VALUES (null,'$date',$str)")
                                val content = "Đã tải xong kết quả ngày: " + MainState.date2DMY
                                Toast.makeText(activity, content, Toast.LENGTH_LONG).show()
                                return@Listener
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (th: Throwable) {
                            if (str.isNotEmpty()) {
                                db!!.queryData("Insert Into ketqua VALUES (null,'$date',$str)")
                                val content = "Đã tải xong kết quả ngày: " + MainState.date2DMY
                                Toast.makeText(activity, content, Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(activity, "Không có kết quả phù hợp!", Toast.LENGTH_LONG).show()
                            }
                            throw th
                        }
                        Toast.makeText(activity, "Không có kết quả phù hợp!", Toast.LENGTH_LONG).show()
                    },
                    Response.ErrorListener { }) {
                    public override fun getParams(): Map<String, String> {
                        return HashMap()
                    }
                })
            } catch (e: Exception) {
                Toast.makeText(activity, "Kiểm tra kết nối mạng!", Toast.LENGTH_LONG).show()
            }
        }
        bind.btnTt.setOnClickListener {
            bind.btnTt.isEnabled = false
            val mDate = MainState.dateYMD
            val mNgay = MainState.date2DMY
            val cursor = db!!.getData("Select * From Ketqua WHERE ngay = '$mDate'")
            cursor.moveToFirst()
            var loadDone = false
            try {
                for (i in 2..28) {
                    if (cursor.isNull(i) || !isNumeric(cursor.getString(i))) break else if (i >= 28) loadDone = true
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "Chưa có kết quả ngày: $mNgay", Toast.LENGTH_SHORT).show()
            }
            if (loadDone) {
                try {
                    TinhTienService.I.run(mDate)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Toast.makeText(activity, "Đã tính tiền xong ngày $mNgay", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Chưa có kết quả ngày $mNgay hãy cập nhật thủ công.", Toast.LENGTH_SHORT)
                    .show()
            }
            if (!cursor.isClosed) {
                cursor.close()
            }
            bind.btnTt.isEnabled = true
        }
        bind.btnDelete.setOnClickListener { view: View? ->
            val popupL = PopupMenu(activity, view)
            arrayOf("Xóa vẫn lưu lại công nợ", "Xóa hết cơ sở dữ liệu", "Xóa hết dữ liệu hôm nay")
                .forEachIndexed { index, s -> popupL.menu.add(1, index, index, s) }

            popupL.setOnMenuItemClickListener { item: MenuItem ->
                when (item.order) {
                    0 -> {
                        delAllSQLCongno()
                    }
                    1 -> {
                        delAllSQL()
                    }
                    2 -> {
                        delAllSQLtoday()
                    }
                }
                true
            }
            popupL.show()
        }
        bind.minhngoc.setOnCheckedChangeListener { _, isChecked ->
            bind.gr1.clearCheck()
            if (isChecked) displayMinhNgoc()
        }
        bind.xosothienphu.setOnCheckedChangeListener { _, isChecked ->
            bind.gr2.clearCheck()
            if (isChecked) displayXsThienPhu()
        }
        bind.xosome.setOnCheckedChangeListener { _, isChecked ->
            bind.gr1.clearCheck()
            if (isChecked) displayXSme()
        }
        bind.xsme.setOnCheckedChangeListener { _, isChecked ->
            bind.gr2.clearCheck()
            if (isChecked) displayXSmeNew()
        }
        bind.xsmn.setOnCheckedChangeListener { _, isChecked ->
            bind.gr2.clearCheck()
            if (isChecked) displayXSMNNew()
        }
        bind.webview.addJavascriptInterface(NotificationBindObject(activity!!.applicationContext), "NotificationBind")
        setUpWebViewDefaults(bind.webview)
        if (savedInstanceState != null) {
            bind.webview.restoreState(savedInstanceState)
        }
        displayXSmeNew()
        return bind.root
    }

    override fun onStop() {
        super.onStop()
        bind.webview.clearCache(true)
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

    fun loadJavascript(javascript: String) {
        bind.webview.evaluateJavascript(javascript) { s: String ->
            var msg = ""
            val reader = JsonReader(StringReader(s))
            reader.isLenient = true
            try {
                if (!(reader.peek() == JsonToken.NULL || reader.peek() != JsonToken.STRING
                            || reader.nextString().also { msg = it } == null)
                ) {
                    giais = msg.trim()
                        .replace("\t", "!").replace("\n", "!")
                        .clearDuplicate('!')
                        .split("!")
                    if (giais.isEmpty()) {
                        Toast.makeText(activity, "Kiểm tra lại kết nối Internet!", Toast.LENGTH_LONG).show()
                    } else if (giais.size > 16) {
                        if (bind.xosome.isChecked) {
                            phantichXoso()
                        } else if (bind.xsme.isChecked) {
                            phantichXosomeNew()
                        } else if (bind.xsmn.isChecked) {
                            phantichXosomeNewNew()
                        } else {
                            phantichXoso()
                        }
                    }
                }

            } catch (e2: IOException) {
            } catch (th: Throwable) {
            }
            try {
                reader.close()
            } catch (ignored: IOException) {
            }
        }
    }

    private fun showDialogDelete(title: String, doPositive: Function<String, String>) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
        var edittext: EditText? = null
        val pass = AkaManSec.userPwd
        if (pass != null && pass.isNotEmpty()) {
            val inflater = LayoutInflater.from(context)
            val layoutPass = inflater.inflate(R.layout.layout_nhap_mat_khau, null)
            edittext = layoutPass.findViewById(R.id.edt_password)
            builder.setView(layoutPass)
        }
        builder.setTitle(title)
        val finalEdittext = edittext
        builder.setPositiveButton("OK") { _, _ ->
            if (finalEdittext != null) {
                val textEncode = AkaManSec.md5(finalEdittext.text.toString().trim())
                if (textEncode != pass) {
                    Toast.makeText(activity, "Mật khẩu không đúng!", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
            }
            doPositive.apply("")
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }

    private fun delAllSQL() {
        showDialogDelete("Xoá hết cơ sở dữ liệu?", Function {
            db!!.queryData("DROP TABLE if exists Chat_database")
            db!!.queryData("DROP TABLE if exists tbl_tinnhanS")
            db!!.queryData("DROP TABLE if exists tbl_soctS")
            db!!.createTinNhanGoc()
            db!!.createSoCT()
            db!!.createTableChat()
            Toast.makeText(activity, "Đã xoá", Toast.LENGTH_LONG).show()
            ""
        })
    }

    private fun delAllSQLCongno() {
        showDialogDelete("Xoá dữ liệu vẫn giữ công nợ?", Function { str: String? ->
            val mTenKH = ArrayList<String>()
            val mSodt = ArrayList<String>()
            val mSoTien = ArrayList<String>()
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val calendar = Calendar.getInstance()
            calendar.add(5, -1)
            val ngay = sdf.format(Date(calendar.timeInMillis))
            val cursor =
                db!!.getData("Select tbl_soctS.ten_kh\n, SUM(tbl_soctS.ket_qua * (100-tbl_soctS.diem_khachgiu)/100)/1000  as NoCu, \ntbl_soctS.so_dienthoai, tbl_kh_new.type_kh  \nFROM tbl_soctS INNER JOIN tbl_kh_new ON tbl_soctS.so_dienthoai = tbl_kh_new.sdt\nGROUP BY tbl_soctS.ten_kh ORDER BY tbl_soctS.type_kh DESC")
            while (cursor.moveToNext()) {
                mTenKH.add(cursor.getString(0))
                mSodt.add(cursor.getString(2))
                mSoTien.add((cursor.getDouble(1) * 1000.0).toString() + "")
            }
            db!!.queryData("DROP TABLE if exists Chat_database")
            db!!.queryData("DROP TABLE if exists tbl_tinnhanS")
            db!!.queryData("DROP TABLE if exists tbl_soctS")
            db!!.createTinNhanGoc()
            db!!.createSoCT()
            db!!.createTableChat()
            for (i in mTenKH.indices) {
                db!!.queryData("Insert Into tbl_soctS (ngay_nhan, ten_kh, so_dienthoai, the_loai, ket_qua) Values ('" + ngay + "','" + mTenKH[i] + "','" + mSodt[i] + "', 'cn'," + mSoTien[i] + ")")
            }
            Toast.makeText(activity, "Đã xoá", Toast.LENGTH_LONG).show()
            ""
        })
    }

    private fun delAllSQLtoday() {
        showDialogDelete("Xoá hết dữ liệu hôm nay?", Function {
            val date = MainState.dateYMD
            db!!.queryData("DELETE FROM tbl_soctS WHERE ngay_nhan = '$date'")
            db!!.queryData("DELETE FROM tbl_tinnhanS WHERE ngay_nhan = '$date'")
            db!!.queryData("DELETE FROM Chat_database WHERE ngay_nhan = '$date'")
            Toast.makeText(activity, "Đã xoá", Toast.LENGTH_LONG).show()
            ""
        })
    }

    private fun displayWebView(url: (date: String) -> String, vararg js: String) {
        bind.webview.visibility = View.GONE
        val date = MainState.date2DMY.replace("/", "-")
        bind.webview.loadUrl(url(date))
        bind.webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                bind.webview.visibility = View.VISIBLE
                for (str in js) {
                    loadJavascript(str)
                }
            }
        }
    }

    private fun displayXSme() {
        val js1 =
            "document.getElementsByClassName('embeded-breadcrumb')[0].style.display = 'none';\ndocument.getElementsByClassName('tit-mien')[0].style.display = 'none';"
        val body2 = if (bind.xosome.isChecked)
            "document.getElementsByClassName('table table-bordered table-striped table-xsmb')[0].innerText;"
        else
            "document.getElementsByClassName('table-result')[0].innerText;"

        displayWebView(
            { date -> "https://xoso.me/xsmb-$date.html" },
            js1,
            "(function() { return $body2; })();"
        )
    }

    private fun displayXSmeNew() = displayWebView(
        { date -> Const.URL_XOSOME_KQMB + "#n" + date },
        "(function() { return " + "document.getElementsByClassName('kqmb extendable')[0].innerText;" + "; })();"
    )

    private fun displayXSMNNew() = displayWebView(
        { date -> "https://xsmn.mobi/embedded/kq-mienbac?ngay_quay=$date" },
        "(function() { return \" + \"document.getElementsByClassName('extendable kqmb colgiai')[0].innerText;\" + \"; })();"
    )

    private fun displayMinhNgoc() = displayWebView(
        { date -> "https://xoso.com.vn/xsmb-$date.html" },
        "(function() { return " + "document.getElementsByClassName('table-result')[0].innerText;" + "; })();"
    )

    private fun displayXsThienPhu() = displayWebView(
        { date -> "https://xosothienphu.com/ma-nhung/xsmb-$date.html" },
        "(function() { return " + "document.getElementsByClassName('table table-condensed kqcenter kqvertimarginw table-kq-border table-kq-hover-div table-bordered kqbackground table-kq-bold-border tb-phoi-border watermark table-striped')[0].innerText;" + "; })();"
    )

    fun phantichXoso() {
        val date = MainState.dateYMD
        val kTra = giais.all { isNumeric(it) || it.length >= 2 }
        if (!kTra) {
            Toast.makeText(activity, "Chưa có kết quả!", Toast.LENGTH_LONG).show()
            return
        }
        try {
            db!!.queryData("Delete From ketqua WHERE ngay = '$date'")

            val giaiStr = giais.filter { isNumeric(it) }.joinToString(",") { "'$it'" }
            db!!.queryData("Insert Into KETQUA VALUES (null,'$date',$giaiStr)")

            Toast.makeText(activity, "Đã tải kết quả ngày: ${MainState.date2DMY}", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
        }
    }

    fun phantichXosomeNew() {
        val date = MainState.dateYMD
        var validResult = true

        if (isNumeric(giais[2]) && isNumeric(giais[4]) && isNumeric(giais[6]) && isNumeric(giais[8])
            && isNumeric(giais[10]) && isNumeric(giais[12]) && isNumeric(giais[14]) && isNumeric(giais[16])
        ) {
            val giaiStr = "'" + giais[2].trim() + "'," +
                    "'" + giais[4].trim() + "'," +
                    "'" + giais[6].trim().substring(0, 5) + "'," +
                    "'" + giais[6].trim().substring(5) + "'," +
                    "'" + giais[8].trim().substring(0, 5) + "'," +
                    "'" + giais[8].trim().substring(5, 10) + "'," +
                    "'" + giais[8].trim().substring(10, 15) + "'," +
                    "'" + giais[8].trim().substring(15, 20) + "'," +
                    "'" + giais[8].trim().substring(20, 25) + "'," +
                    "'" + giais[8].trim().substring(25) + "'," +
                    "'" + giais[10].trim().substring(0, 4) + "'," +
                    "'" + giais[10].trim().substring(4, 8) + "'," +
                    "'" + giais[10].trim().substring(8, 12) + "'," +
                    "'" + giais[10].trim().substring(12) + "'," +
                    "'" + giais[12].trim().substring(0, 4) + "'," +
                    "'" + giais[12].trim().substring(4, 8) + "'," +
                    "'" + giais[12].trim().substring(8, 12) + "'," +
                    "'" + giais[12].trim().substring(12, 16) + "'," +
                    "'" + giais[12].trim().substring(16, 20) + "'," +
                    "'" + giais[12].trim().substring(20) + "'," +
                    "'" + giais[14].trim().substring(0, 3) + "'," +
                    "'" + giais[14].trim().substring(3, 6) + "'," +
                    "'" + giais[14].trim().substring(6) + "'," +
                    "'" + giais[16].trim().substring(0, 2) + "'," +
                    "'" + giais[16].trim().substring(2, 4) + "'," +
                    "'" + giais[16].trim().substring(4, 6) + "'," +
                    "'" + giais[16].trim().substring(6) + "'"

            if (giaiStr.length > 185) {
                db?.let {
                    it.queryData("Delete From ketqua WHERE ngay = '$date'")
                    it.queryData("InSert Into KETQUA VALUES(null,'$date',$giaiStr)")
                }
                val content = "Đã tải xong kết quả ngày: " + MainState.date2DMY
                Toast.makeText(activity, content, Toast.LENGTH_LONG).show()
            }
        } else {
            validResult = false
        }

        if (!validResult) {
            Toast.makeText(activity, "Không có kết quả phù hợp!", Toast.LENGTH_LONG).show()
        }
    }


    fun phantichXosomeNewNew() {
        val date = MainState.dateYMD
        try {
            if (isNumeric(giais[3]) && isNumeric(giais[5]) && isNumeric(giais[7]) && isNumeric(giais[9])
                && isNumeric(giais[11]) && isNumeric(giais[13]) && isNumeric(giais[15]) && isNumeric(giais[17])
            ) {
                val str = "'" + giais[3].trim() + "'," +
                        "'" + giais[5].trim() + "'," +
                        "'" + giais[7].trim().substring(0, 5) + "'," +
                        "'" + giais[7].trim().substring(5) + "'," +
                        "'" + giais[9].trim().substring(0, 5) + "'," +
                        "'" + giais[9].trim().substring(5, 10) + "'," +
                        "'" + giais[9].trim().substring(10, 15) + "'," +
                        "'" + giais[9].trim().substring(15, 20) + "'," +
                        "'" + giais[9].trim().substring(20, 25) + "'," +
                        "'" + giais[9].trim().substring(25) + "'," +
                        "'" + giais[11].trim().substring(0, 4) + "'," +
                        "'" + giais[11].trim().substring(4, 8) + "'," +
                        "'" + giais[11].trim().substring(8, 12) + "'," +
                        "'" + giais[11].trim().substring(12) + "'," +
                        "'" + giais[13].trim().substring(0, 4) + "'," +
                        "'" + giais[13].trim().substring(4, 8) + "'," +
                        "'" + giais[13].trim().substring(8, 12) + "'," +
                        "'" + giais[13].trim().substring(12, 16) + "'," +
                        "'" + giais[13].trim().substring(16, 20) + "'," +
                        "'" + giais[13].trim().substring(20) + "'," +
                        "'" + giais[15].trim().substring(0, 3) + "'," +
                        "'" + giais[15].trim().substring(3, 6) + "'," +
                        "'" + giais[15].trim().substring(6) + "'," +
                        "'" + giais[17].trim().substring(0, 2) + "'," +
                        "'" + giais[17].trim().substring(2, 4) + "'," +
                        "'" + giais[17].trim().substring(4, 6) + "'," +
                        "'" + giais[17].trim().substring(6) + "'"

                if (str.length > 185) {
                    db?.let {
                        it.queryData("Delete From ketqua WHERE ngay = '$date'")
                        it.queryData("InSert Into KETQUA VALUES(null,'$date',$str)")
                    }
                    Toast.makeText(activity, "Đã tải kết quả ngày: ${MainState.date2DMY}", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(activity, "Không có kết quả phù hợp!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(activity, "Không có kết quả phù hợp!", Toast.LENGTH_LONG).show()
            }
        } catch (ignored: Throwable) {
        }
    }
}