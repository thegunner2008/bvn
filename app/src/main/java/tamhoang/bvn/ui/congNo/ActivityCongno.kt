package tamhoang.bvn.ui.congNo

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.model.SoctS
import tamhoang.bvn.data.store.SoctStore
import tamhoang.bvn.databinding.ActivityCongnoBinding
import tamhoang.bvn.databinding.ActivityCongnoLvBinding
import tamhoang.bvn.databinding.DialogNhapCongNoBinding
import tamhoang.bvn.databinding.FragThanhToanBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.util.extensions.formatDate
import tamhoang.bvn.util.extensions.toDecimal
import tamhoang.bvn.util.ld.CongThuc.isNumeric
import java.text.DecimalFormat
import java.util.*

class ActivityCongno() : BaseToolBarActivity() {
    private var _bind: ActivityCongnoBinding? = null
    private val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var mKetQua = ArrayList<String>()
    var mluyKe = ArrayList<String>()
    var mNgay = ArrayList<String>()
    var mNgayNhan = ArrayList<String>()
    var mSdt = ArrayList<String>()
    var mThanhToan = ArrayList<String>()
    lateinit var tenKH: String

    override fun getLayoutId(): Int {
        return R.layout.activity_congno
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityCongnoBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)
        tenKH = intent.getStringExtra("tenKH") ?: return

        bind.tvTenKH.text = "Khách hàng: $tenKH"
        bind.btnCongno.setOnClickListener {
            if (!this@ActivityCongno.isFinishing) {
                dialogNhapCongNoBanDau()
            }
        }
        bind.lvCongno.onItemClickListener = OnItemClickListener { _, _, i, _ ->
            if (!this@ActivityCongno.isFinishing) {
                showDialogThanhToan(i)
            }
        }
        xemCongnoLv()
    }

    private fun dialogNhapCongNoBanDau() {
        val dialog = Dialog(this)
        val bindDialog = DialogNhapCongNoBinding.inflate(layoutInflater)
        dialog.setContentView(bindDialog.root)
        dialog.window!!.setLayout(-1, -2)
        val cursor =
            db!!.getData("Select sum(ket_qua)/1000 From tbl_soctS Where ten_kh = '$tenKH' AND the_loai = 'cn'")
        cursor.moveToFirst()
        fun textThanhToan() = bindDialog.edtThanhtoan.text.toString()

        bindDialog.edtThanhtoan.setText(cursor.getDouble(0).toDecimal())
        if (!cursor.isClosed) {
            cursor.close()
        }
        bindDialog.edtThanhtoan.addTextChangedListener(object : TextWatcher {
            var length = 0
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                length = textThanhToan().length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (textThanhToan().isEmpty()) {
                    bindDialog.edtThanhtoan.setText("0")
                } else if (length != textThanhToan().length && length > 2) {
                    try {
                        val soStr = textThanhToan().replace("[$,.]".toRegex(), "").toDouble().toDecimal()
                        bindDialog.edtThanhtoan.setText(soStr)
                        bindDialog.edtThanhtoan.setSelection(soStr.length)
                    } catch (e: Exception) {
                    }
                }
            }
        })
        bindDialog.btnChinhsua.setOnClickListener {
            if (isNumeric(textThanhToan().replace(".", "").replace("-", ""))) {
                val countId = SoctStore.I.countWhere("ten_kh = '$tenKH' AND the_loai = 'cn'")
                val textThanhToan = textThanhToan().replace(",", "")
                if (countId == 0) {
                    val cursor1 =
                        db!!.getData("Select min(ngay_nhan), so_dienthoai From tbl_soctS Where ten_kh = '$tenKH'")
                    cursor1.moveToFirst()

                    val ngay = cursor1.getString(0).formatDate(action = { it.add(Calendar.DATE, -1) })
                    SoctStore.I.insert(
                        SoctS.toUpdateValues(
                            ngay_nhan = ngay, ten_kh = tenKH, so_dienthoai = cursor1.getString(1), the_loai = "cn",
                            ket_qua = textThanhToan.toDouble() * 1000, diem_quydoi = 1.0
                        )
                    )
                    if (!cursor1.isClosed) {
                        cursor1.close()
                    }
                } else {
                    db!!.queryData(
                        "Update tbl_soctS set ket_qua = ${textThanhToan}000 WHere ten_kh = '$tenKH' AND the_loai = 'cn'"
                    )
                }
                xemCongnoLv()
                dialog.cancel()
            }
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun showDialogThanhToan(poin: Int) {
        val dialog = Dialog(this)
        val bindDialog = FragThanhToanBinding.inflate(layoutInflater)
        dialog.setContentView(bindDialog.root)
        dialog.window!!.setLayout(-1, -2)
        bindDialog.tvNgaytt.text = mNgay[poin]

        fun textThanhtoan() = bindDialog.edtThanhtoan.text.toString()
        bindDialog.edtThanhtoan.addTextChangedListener(object : TextWatcher {
            var length = 0
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                length = bindDialog.edtThanhtoan.text.toString().length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (textThanhtoan().isEmpty()) {
                    bindDialog.edtThanhtoan.setText("0")
                } else if (length != textThanhtoan().length && length > 2) {
                    try {
                        val strSo = textThanhtoan().replace("[$,.]", "").toDouble().toDecimal()
                        bindDialog.edtThanhtoan.setText(strSo)
                        bindDialog.edtThanhtoan.setSelection(strSo.length)
                    } catch (e: Exception) {
                    }
                }
            }
        })
        val cursor =
            db!!.getData("Select sum(ket_qua)/1000 From tbl_soctS Where ten_kh = '$tenKH' AND the_loai = 'tt' And ngay_nhan = '${mNgayNhan[poin]}'")
        cursor.moveToFirst()
        bindDialog.edtThanhtoan.setText(cursor.getDouble(0).toDecimal())
        if (!cursor.isClosed) {
            cursor.close()
        }
        bindDialog.btnChinhsua.setOnClickListener {
            if (isNumeric(textThanhtoan().replace(".", "").replace("-", ""))) {
                val cursorCount =
                    db!!.getData("Select count(id) From tbl_soctS Where ten_kh = '$tenKH' AND the_loai = 'tt' AND ngay_nhan = '${mNgayNhan[poin]}'")
                cursorCount.moveToFirst()
                if (cursorCount.getInt(0) == 0) {
                    SoctStore.I.insert(
                        SoctS.toUpdateValues(
                            ngay_nhan = mNgayNhan[poin], ten_kh = tenKH, so_dienthoai = mSdt[poin], the_loai = "tt",
                            ket_qua = textThanhtoan().toDouble() * 1000, diem_quydoi = 1.0
                        )
                    )
                } else {
                    db!!.queryData(
                        "Update tbl_soctS set ket_qua = ${
                            textThanhtoan().replace(".", "")
                        }000 Where ten_kh = '$tenKH' AND the_loai = 'tt' AND ngay_nhan = '${mNgayNhan[poin]}'"
                    )
                }
                xemCongnoLv()

                if (!cursorCount.isClosed) {
                    cursorCount.close()
                }
                dialog.cancel()
            }
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun xemCongnoLv() {
        val decimalFormat = DecimalFormat("###,###")
        mNgayNhan.clear()
        mSdt.clear()
        mNgay.clear()
        mKetQua.clear()
        mThanhToan.clear()
        mluyKe.clear()
        val cursor =
            db!!.getData("Select ngay_nhan, so_dienthoai,strftime('%d/%m/%Y',ngay_nhan) as Ngay\n, sum((the_loai <> 'tt') *ket_qua*(100 - diem_khachgiu)/100)/1000 as KQ \n, sum((the_loai = 'tt') *ket_qua)/1000 as TT \n, (Select sum(ket_qua*(100 - diem_khachgiu)/100) FROM tbl_soctS t2 \nWHERE tbl_soctS.ngay_nhan >= t2.ngay_nhan And tbl_soctS.ten_kh = t2.ten_kh)/1000 AS luy_ke \nFROM tbl_soctS \nWHERE ten_kh = '$tenKH' \nGROUP BY ngay_nhan ORDER BY ngay_nhan")
        while (cursor.moveToNext()) {
            mNgayNhan.add(cursor.getString(0))
            mSdt.add(cursor.getString(1))
            mNgay.add(cursor.getString(2))
            mKetQua.add(decimalFormat.format(cursor.getDouble(3)))
            mThanhToan.add(decimalFormat.format(cursor.getDouble(4)))
            mluyKe.add(decimalFormat.format(cursor.getDouble(5)))
        }
        bind.lvCongno.adapter = CongnoAdapter(this, R.layout.activity_congno_lv, mNgay)
    }

    inner class CongnoAdapter(context: Context?, resource: Int, objects: List<String?>?) : ArrayAdapter<Any?>(
        (context)!!, resource, (objects)!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val bindLv = ActivityCongnoLvBinding.inflate(layoutInflater)
            bindLv.tvNgayThanhtoan.text = mNgay[position]
            bindLv.tvPhatsinh.text = mKetQua[position]
            bindLv.tvThanhtoan.text = mThanhToan[position]
            bindLv.tvLuyke.text = mluyKe[position]
            return bindLv.root
        }
    }
}