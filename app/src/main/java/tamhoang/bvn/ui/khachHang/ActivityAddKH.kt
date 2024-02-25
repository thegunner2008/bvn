package tamhoang.bvn.ui.khachHang

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.ActivityAddKhBinding
import tamhoang.bvn.ui.base.dialog.Dialog
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.ld.CongThuc.isNumeric

class ActivityAddKH : BaseToolBarActivity() {
    private var _bind: ActivityAddKhBinding? = null
    private val bind get() = _bind!!

    var caidatGia: JSONObject? = null
    var caidatTg: JSONObject? = null
    var cursor: Cursor? = null
    var db: DbOpenHelper? = null
    var json: JSONObject? = null
    var jsonKhongmax: JSONObject? = null
    var soDienthoai: String? = null
    var tenKhach: String? = null
    var appUse: String? = null
    var type = 0

    override fun getLayoutId() = R.layout.activity_add_kh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityAddKhBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)
        val intent = intent
        tenKhach = intent.getStringExtra("tenKH")
        soDienthoai = intent.getStringExtra("so_dienthoai")
        appUse = intent.getStringExtra("use_app")
        var demKhach = 0
        if (tenKhach!!.isNotEmpty()) {
            bind.edtTen.setText(tenKhach)
            bind.edtSdt.setText(soDienthoai)
            cursor = db!!.getData("Select * From tbl_kh_new where ten_kh = '$tenKhach'")
            cursor!!.moveToFirst()
            demKhach = cursor!!.count
        }
        if (demKhach > 0) {
            bind.edtSdt.setText(cursor!!.getString(1))
            if (cursor!!.getString(2).indexOf("sms") == -1) {
                bind.linnerSodienthoai.isEnabled = false
                bind.edtTen.isEnabled = false
                bind.edtSdt.isEnabled = false
                bind.btnDanhba.isEnabled = false
            }
            if (cursor!!.count > 0) {
                try {
                    json = JSONObject(cursor!!.getString(5))
                    caidatGia = json!!.getJSONObject("caidat_gia")
                    bind.edtGiadea.setText(caidatGia!!.getString("dea"))
                    bind.edtAndea.setText(caidatGia!!.getString("an_dea"))
                    bind.edtGiadeb.setText(caidatGia!!.getString("deb"))
                    bind.edtAndeb.setText(caidatGia!!.getString("an_deb"))
                    bind.edtGiadec.setText(caidatGia!!.getString("dec"))
                    bind.edtAndec.setText(caidatGia!!.getString("an_dec"))
                    bind.edtGiaded.setText(caidatGia!!.getString("ded"))
                    bind.edtAnded.setText(caidatGia!!.getString("an_ded"))
                    bind.edtGiadet.setText(caidatGia!!.getString("det"))
                    bind.edtAndet.setText(caidatGia!!.getString("an_det"))
                    bind.edtGiaLo.setText(caidatGia!!.getString("lo"))
                    bind.edtAnLo.setText(caidatGia!!.getString("an_lo"))
                    bind.edtGiaXien2.setText(caidatGia!!.getString("gia_x2"))
                    bind.edtAnXien2.setText(caidatGia!!.getString("an_x2"))
                    bind.edtGiaXien3.setText(caidatGia!!.getString("gia_x3"))
                    bind.edtAnXien3.setText(caidatGia!!.getString("an_x3"))
                    bind.edtGiaXien4.setText(caidatGia!!.getString("gia_x4"))
                    bind.edtAnXien4.setText(caidatGia!!.getString("an_x4"))
                    bind.edtGiaXienNhay.setText(caidatGia!!.getString("gia_xn"))
                    bind.edtAnXienNhay.setText(caidatGia!!.getString("an_xn"))
                    bind.edtGia3c.setText(caidatGia!!.getString("gia_bc"))
                    bind.edtAn3c.setText(caidatGia!!.getString("an_bc"))
                    if (cursor!!.getInt(3) == 1) {
                        bind.radKhach.isChecked = true
                        bind.radChu.isChecked = false
                        bind.radChuKhach.isChecked = false
                    } else if (cursor!!.getInt(3) == 2) {
                        bind.radKhach.isChecked = false
                        bind.radChu.isChecked = true
                        bind.radChuKhach.isChecked = false
                    } else if (cursor!!.getInt(3) == 3) {
                        bind.radKhach.isChecked = false
                        bind.radChu.isChecked = false
                        bind.radChuKhach.isChecked = true
                    }
                    jsonKhongmax = JSONObject(cursor!!.getString(6))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val cursor2 = cursor
                if (cursor2 != null && !cursor2.isClosed) {
                    cursor!!.close()
                }
            }
        } else if (!appUse!!.contains("sms")) {
            bind.edtTen.setText(tenKhach)
            bind.edtSdt.setText(soDienthoai)
            bind.edtTen.isEnabled = false
            bind.edtSdt.isEnabled = false
            bind.btnDanhba.isEnabled = false
        }
        bind.btnDanhba.setOnClickListener { v: View? ->
            startActivityForResult(
                Intent(
                    "android.intent.action.PICK",
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                ), 2015
            )
        }
        bind.btnThemKH.setOnClickListener {
            val tenKh = bind.edtTen.text.toString()
            var sdt = bind.edtSdt.text.toString()

            if (sdt.isNotEmpty() && tenKh.isNotEmpty()) {
                if (sdt.startsWith("0") && isNumeric(sdt)) {
                    sdt = "+84" + sdt.substring(1)
                }
                cursor =
                    db!!.getData("Select * From tbl_kh_new Where ten_kh <> '$tenKh' AND sdt = '$sdt'")
                if (cursor!!.count > 0) {
                    Dialog.simple(
                        this,
                        "Đã có số SĐT này!",
                        "Mỗi khách hàng chỉ được dùng 1 số điện thoại và mỗi số điện thoại chỉ dùng cho 1 khách hàng.",
                        positiveText = "Ok",
                        cancelable = false
                    )
                } else {
                    if (bind.radKhach.isChecked) {
                        type = 1
                    }
                    if (bind.radChu.isChecked) {
                        type = 2
                    }
                    if (bind.radChuKhach.isChecked) {
                        type = 3
                    }
                    if (appUse == null) {
                        appUse = "sms"
                    }
                    cursor = db!!.getData("Select * From tbl_kh_new Where ten_kh = '$tenKh'")
                    cursor!!.moveToFirst()
                    if (cursor!!.count > 0) {
                        try {
                            json = JSONObject(cursor!!.getString(5))
                            caidatGia = json!!.getJSONObject("caidat_gia")
                            caidatTg = json!!.getJSONObject("caidat_tg")
                            appUse = cursor!!.getString(2)
                            jsonKhongmax = JSONObject(cursor!!.getString(6))
                        } catch (e: JSONException) {
                        }
                    } else {
                        json = JSONObject()
                        caidatGia = JSONObject()
                        caidatTg = JSONObject()
                        jsonKhongmax = JSONObject()
                        try {
                            caidatTg!!.apply {
                                put("dlgiu_de", 0)
                                put("dlgiu_lo", 0)
                                put("dlgiu_xi", 0)
                                put("dlgiu_xn", 0)
                                put("dlgiu_bc", 0)
                                put("khgiu_de", 0)
                                put("khgiu_lo", 0)
                                put("khgiu_xi", 0)
                                put("khgiu_xn", 0)
                                put("khgiu_bc", 0)
                                put("ok_tin", 3)
                                put("xien_nhan", 0)
                                put("chot_sodu", 0)
                                put("tg_loxien", "18:13")
                                put("tg_debc", "18:20")
                                put("loi_donvi", 0)
                                put("heso_de", 0)
                                put("maxDe", 0)
                                put("maxLo", 0)
                                put("maxXi", 0)
                                put("maxCang", 0)
                            }
                            jsonKhongmax!!.apply {
                                put("danDe", "")
                                put("danDauDB", "")
                                put("danDauG1", "")
                                put("danDitG1", "")
                                put("danLo", "")
                                put("soDe", JSONObject().toString())
                                put("soDauDB", JSONObject().toString())
                                put("soDauG1", JSONObject().toString())
                                put("soDitG1", JSONObject().toString())
                                put("soLo", JSONObject().toString())
                                put("xien2", 0)
                                put("xien3", 0)
                                put("xien4", 0)
                                put("cang", 0)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    try {
                        caidatGia!!.apply {
                            put("dea", bind.edtGiadea.text.toString())
                            put("an_dea", bind.edtAndea.text.toString())
                            put("deb", bind.edtGiadeb.text.toString())
                            put("an_deb", bind.edtAndeb.text.toString())
                            put("det", bind.edtGiadet.text.toString())
                            put("an_det", bind.edtAndet.text.toString())
                            put("dec", bind.edtGiadec.text.toString())
                            put("an_dec", bind.edtAndec.text.toString())
                            put("ded", bind.edtGiaded.text.toString())
                            put("an_ded", bind.edtAnded.text.toString())
                            put("lo", bind.edtGiaLo.text.toString())
                            put("an_lo", bind.edtAnLo.text.toString())
                            put("gia_x2", bind.edtGiaXien2.text.toString())
                            put("an_x2", bind.edtAnXien2.text.toString())
                            put("gia_x3", bind.edtGiaXien3.text.toString())
                            put("an_x3", bind.edtAnXien3.text.toString())
                            put("gia_x4", bind.edtGiaXien4.text.toString())
                            put("an_x4", bind.edtAnXien4.text.toString())
                            put("gia_xn", bind.edtGiaXienNhay.text.toString())
                            put("an_xn", bind.edtAnXienNhay.text.toString())
                            put("gia_bc", bind.edtGia3c.text.toString())
                            put("an_bc", bind.edtAn3c.text.toString())
                        }
                        json!!.put("caidat_gia", caidatGia)
                        json!!.put("caidat_tg", caidatTg)

                        db!!.queryData(
                            "REPLACE Into tbl_kh_new Values ('$tenKh','$sdt','$appUse',$type,0,'$json','$jsonKhongmax')"
                        )
                        if (type != 2) {
                            try {
                                db!!.queryData("Delete FROM tbl_chuyenthang WHERE sdt_nhan = '${bind.edtSdt.text}'")
                            } catch (e: Exception) {
                                Toast.makeText(this, "Sai số liệu, hãy kiểm tra lại", Toast.LENGTH_LONG).show()
                                MainState.refreshDsKhachHang()
                                finish()
                            }
                        }
                        if (cursor!!.count <= 0) {
                            Toast.makeText(this@ActivityAddKH, "Đã cập nhật thông tin!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@ActivityAddKH, "Đã thêm khách hàng!", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onCreate: ${e.message}")
                        Toast.makeText(this@ActivityAddKH, "Sai số liệu, hãy kiểm tra lại", Toast.LENGTH_LONG).show()
                        MainState.refreshDsKhachHang()
                        finish()
                    }
                    MainState.refreshDsKhachHang()
                    finish()
                }
            }
        }
    }

    @SuppressLint("Range")
    public override fun onActivityResult(i: Int, i2: Int, intent: Intent?) {
        super.onActivityResult(i, i2, intent)
        if (i == 2015 && i2 == -1) {
            val query = contentResolver.query(intent!!.data!!, null, null, null, null)
            query!!.moveToFirst()
            val columnIndex = query.getColumnIndex("data1")
            query.getColumnIndex("display_name")
            val name = query.getString(query.getColumnIndex("display_name"))
            var sdt = query.getString(columnIndex).replace(" ".toRegex(), "")
            if (sdt.length < 12) {
                sdt = "+84" + sdt.substring(1)
            }
            bind.edtSdt.setText(sdt)
            bind.edtTen.setText(name)
            query.close()
        }
    }
}