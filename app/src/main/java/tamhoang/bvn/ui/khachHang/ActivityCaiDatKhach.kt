package tamhoang.bvn.ui.khachHang

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SeekBar.OnSeekBarChangeListener
import org.json.JSONException
import org.json.JSONObject
import tamhoang.bvn.R
import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.setting.SettingKH
import tamhoang.bvn.data.setting.TLGiu
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.databinding.ActivityCaiDatKhachBinding
import tamhoang.bvn.databinding.FragKhongmaxBinding
import tamhoang.bvn.ui.base.toolbar.BaseToolBarActivity
import tamhoang.bvn.util.extensions.convertLatin
import tamhoang.bvn.util.extensions.to2ChuSo
import tamhoang.bvn.util.extensions.toDecimal
import java.util.*

class ActivityCaiDatKhach : BaseToolBarActivity() {
    private var _bind: ActivityCaiDatKhachBinding? = null
    private val bind get() = _bind!!
    private val controller = KhachHangController()
    var db: DbOpenHelper? = null

    lateinit var settingKH: SettingKH

    private var jsonSetting: JSONObject? = null
    var jsonKhongMax: JSONObject? = null
    var tenKH: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_cai_dat_khach
    }

    private fun createSpinner(spinner: Spinner, model: SettingKH.Model) {
        spinner.adapter = ArrayAdapter(this, R.layout.spinner_item, model.list)
        spinner.setSelection(model.value())
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                model.save(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createSeekbarDaily(seekBar: SeekBar, textView: TextView, tl: TLGiu) {
        val value = settingKH.daiLyGiu[tl.toString()] ?: 0
        textView.text = value.toDecimal() + "%"
        seekBar.progress = value / 5
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var max = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = (progress * 5).toString() + "%"
                max = progress * 5
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                try {
                    settingKH.putDaiLyGiu(tl, max)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Toast.makeText(this@ActivityCaiDatKhach, "Mình giữ $max%", Toast.LENGTH_LONG).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun createSeekbarKhach(seekBar: SeekBar, textView: TextView, tl: TLGiu) {
        val value = settingKH.khachGiu[tl.toString()] ?: 0
        textView.text = value.toDecimal() + "%"
        seekBar.progress = value / 5
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var max = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = (progress * 5).toString() + "%"
                max = progress * 5
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                try {
                    settingKH.putKhachGiu(tl, max)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Toast.makeText(this@ActivityCaiDatKhach, "Giữ cho khách $max%", Toast.LENGTH_LONG).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _bind = ActivityCaiDatKhachBinding.inflate(layoutInflater)
        setContentView(bind.root)
        db = DbOpenHelper(this)
        tenKH = intent.getStringExtra("tenKH") ?: return

        val khach = KhachHangStore.I.selectByName(tenKH!!) ?: return
        jsonSetting = JSONObject(khach.tbl_MB)
        val caidatTg = jsonSetting!!.getJSONObject("caidat_tg") ?: return
        settingKH = SettingKH(caidatTg)

        bind.edtTen.text = tenKH

        createSpinner(bind.spTraloitn, settingKH.traLoiTin)
        createSpinner(bind.spNhanXien, settingKH.nhanXien)
        createSpinner(bind.spChotSodu, settingKH.chotSoDu)
        createSpinner(bind.spBaoloidonvi, settingKH.baoLoiDonVi)
        createSpinner(bind.spKhachde, settingKH.khachDe)
        createSpinner(bind.spHesode, settingKH.heSoDe)

        try {
            createSeekbarDaily(bind.seekGiuDedly, bind.ptGiuDeDly, TLGiu.De)
            createSeekbarDaily(bind.seekGiuLodly, bind.ptGiuLoDly, TLGiu.Lo)
            createSeekbarDaily(bind.seekGiu3cdly, bind.ptGiuBcDly, TLGiu.Bc)
            createSeekbarDaily(bind.seekGiuXidly, bind.ptGiuXiDly, TLGiu.Xi)

            createSeekbarKhach(bind.seekGiuDekhach, bind.ptGiuDeKhach, TLGiu.De)
            createSeekbarKhach(bind.seekGiuLokhach, bind.ptGiuLoKhach, TLGiu.Lo)
            createSeekbarKhach(bind.seekGiu3ckhach, bind.ptGiuBcKhach, TLGiu.Bc)
            createSeekbarKhach(bind.seekGiuXikhach, bind.ptGiuXiKhach, TLGiu.Xi)

            jsonKhongMax = JSONObject(khach.tbl_XS)
            var danGiuDe = if (jsonKhongMax!!.getString("danDe").isEmpty())
                "  Đề: Không khống"
            else
                "  Đề: " + jsonKhongMax!!.getString("danDe")

            if (jsonKhongMax!!.getString("danDauDB").isNotEmpty())
                danGiuDe = "$danGiuDe\n  Đầu DB giữ: Không khống"

            if (jsonKhongMax!!.getString("danDauG1").isNotEmpty())
                danGiuDe = "$danGiuDe\n  Đầu G1 giữ: Không khống"

            if (jsonKhongMax!!.getString("danDitG1").isNotEmpty())
                danGiuDe = "$danGiuDe\n  Đít G1 giữ: Không khống"

            val danGiuLo: String = if (jsonKhongMax!!.getString("danLo").isEmpty())
                " Lô: Không khống"
            else
                " Lô: ${jsonKhongMax!!.getString("danLo")}"

            bind.tvKhongMax.text = """$danGiuDe
            $danGiuLo
            Xiên 2: ${jsonKhongMax!!.getString("xien2")}
            Xiên 3: ${jsonKhongMax!!.getString("xien3")}
            Xiên 4: ${jsonKhongMax!!.getString("xien4")}
            Càng: ${jsonKhongMax!!.getString("cang")}""".trimIndent()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        bind.btnExitKH2.setOnClickListener {
            try {
                jsonSetting!!.put("caidat_tg", settingKH.json)
                db!!.queryData("update tbl_kh_new set tbl_MB = '${jsonSetting.toString()}', tbl_XS = '${jsonKhongMax.toString()}' WHERE ten_kh = '$tenKH'")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            finish()
        }
        bind.tvLoXien.text = settingKH.tgLoXien
        bind.tvLoXien.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val mTimePicker =
                TimePickerDialog(this, { _, selectedHour: Int, selectedMinute: Int ->
                    bind.tvLoXien.text = "$selectedHour:${selectedMinute.to2ChuSo()}"
                    Toast.makeText(
                        this,
                        "Đặt không nhận lô, xiên sau: $selectedHour:${selectedMinute.to2ChuSo()}",
                        Toast.LENGTH_LONG
                    ).show()
                    settingKH.tgLoXien = bind.tvLoXien.text.toString()
                }, mcurrentTime[11], mcurrentTime[12], true)
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }
        bind.tvDeCang.text = settingKH.tgDeBc
        bind.tvDeCang.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val mTimePicker =
                TimePickerDialog(this, { _, selectedHour: Int, selectedMinute: Int ->
                    bind.tvDeCang.text = "$selectedHour:${selectedMinute.to2ChuSo()}"
                    Toast.makeText(
                        this,
                        "Đặt không nhận đề/càng sau: $selectedHour:${selectedMinute.to2ChuSo()}",
                        Toast.LENGTH_LONG
                    ).show()
                    settingKH.tgDeBc = bind.tvDeCang.text.toString()
                }, mcurrentTime[11], mcurrentTime[12], true)
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()

        }
        bind.tvKhongMax.setOnClickListener { showDialog2() }
    }

    private fun showDialog2() {
        val bindDialog = FragKhongmaxBinding.inflate(layoutInflater)
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(bindDialog.root)
        try {
            bindDialog.edtNhapDanDe.setText(jsonKhongMax!!.getString("danDe"))
            bindDialog.edtNhapDanDeDauDB.setText(jsonKhongMax!!.getString("danDauDB"))
            bindDialog.edtNhapDanDeDauG1.setText(jsonKhongMax!!.getString("danDauG1"))
            bindDialog.edtNhapDanDeDitG1.setText(jsonKhongMax!!.getString("danDitG1"))
            bindDialog.edtNhapDanLo.setText(jsonKhongMax!!.getString("danLo"))
            bindDialog.giuxien2.setText(jsonKhongMax!!.getString("xien2"))
            bindDialog.giuxien3.setText(jsonKhongMax!!.getString("xien3"))
            bindDialog.giuxien4.setText(jsonKhongMax!!.getString("xien4"))
            bindDialog.giu3cang.setText(jsonKhongMax!!.getString("cang"))
        } catch (e2: JSONException) {
        }
        bindDialog.btnKhongDe.setOnClickListener {
            var str = "de " + bindDialog.edtNhapDanDe.text.toString()
            if (str.length > 7) {
                try {
                    str = controller.parseSo(str.convertLatin()).replace("de dit db:", "de:")
                    if (str.contains(KHONG_HIEU)) {
                        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Thêm bị lỗi, hãy sửa lại 1", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            try {
                if (str.length > 7) {
                    jsonKhongMax!!.put("danDe", bindDialog.edtNhapDanDe.text.toString().replace("\n", " "))
                    val jsonSole = JSONObject()
                    while (str.isNotEmpty()) {
                        val line = str.substring(0, str.indexOf("\n") + 1)
                        val noiDung = str.substring(line.indexOf(":") + 1, line.indexOf("\n") + 1)
                        val daySo = noiDung.substring(0, noiDung.indexOf(",x")).split(",")
                        val soTien = line.substring(line.indexOf(",x") + 2, line.indexOf("\n"))

                        daySo.forEach { soChon ->
                            if (!jsonSole.has(soChon) || jsonSole.getInt(soChon) > soTien.toInt()) {
                                jsonSole.put(soChon, soTien)
                            }
                        }

                        str = str.replace(line, "")
                    }
                    jsonKhongMax!!.put("soDe", jsonSole.toString())
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            update()
        }
        bindDialog.btnXoaDe.setOnClickListener {
            try {
                jsonKhongMax!!.put("danDe", "")
                jsonKhongMax!!.put("soDe", JSONObject().toString())
                bindDialog.edtNhapDanDe.setText("")
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }

        bindDialog.btnKhongDeDauDB.setOnClickListener {
            var str = "dea " + bindDialog.edtNhapDanDeDauDB.text.toString()
            if (str.length > 7) {
                try {
                    str = controller.parseSo(str.convertLatin()).replace("de dau db:", "dea:")
                    if (str.contains(KHONG_HIEU)) {
                        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Thêm bị lỗi, hãy sửa lại 1", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            try {
                if (str.length > 7) {
                    jsonKhongMax!!.put("danDauDB", bindDialog.edtNhapDanDeDauDB.text.toString().replace("\n", " "))
                    val jsonSole = JSONObject()
                    while (str.isNotEmpty()) {
                        val line = str.substring(0, str.indexOf("\n") + 1)
                        val noiDung = str.substring(line.indexOf(":") + 1, line.indexOf("\n") + 1)
                        val daySo = noiDung.substring(0, noiDung.indexOf(",x")).split(",")
                        val soTien = line.substring(line.indexOf(",x") + 2, line.indexOf("\n"))

                        daySo.forEach { soChon ->
                            if (!jsonSole.has(soChon) || jsonSole.getInt(soChon) > soTien.toInt()) {
                                jsonSole.put(soChon, soTien)
                            }
                        }

                        str = str.replace(line, "")
                    }
                    jsonKhongMax!!.put("soDauDB", jsonSole.toString())
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            update()
        }
        bindDialog.btnXoaDeDauDB.setOnClickListener {
            try {
                jsonKhongMax!!.put("danDauDB", "")
                jsonKhongMax!!.put("soDauDB", JSONObject().toString())
                bindDialog.edtNhapDanDeDauDB.setText("")
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }

        bindDialog.btnKhongDeDauG1.setOnClickListener {
            var str = "dec " + bindDialog.edtNhapDanDeDauG1.text.toString()
            if (str.length > 7) {
                try {
                    str = controller.parseSo(str.convertLatin()).replace("de dau nhat:", "dec:")
                    if (str.contains(KHONG_HIEU)) {
                        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Thêm bị lỗi, hãy sửa lại 1", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            try {
                if (str.length > 7) {
                    jsonKhongMax!!.put("danDauG1", bindDialog.edtNhapDanDeDauG1.text.toString().replace("\n", " "))
                    val jsonSole = JSONObject()
                    while (str.isNotEmpty()) {
                        val line = str.substring(0, str.indexOf("\n") + 1)
                        val noiDung = str.substring(line.indexOf(":") + 1, line.indexOf("\n") + 1)
                        val daySo = noiDung.substring(0, noiDung.indexOf(",x")).split(",")
                        val soTien = line.substring(line.indexOf(",x") + 2, line.indexOf("\n"))

                        daySo.forEach { soChon ->
                            if (!jsonSole.has(soChon) || jsonSole.getInt(soChon) > soTien.toInt()) {
                                jsonSole.put(soChon, soTien)
                            }
                        }

                        str = str.replace(line, "")
                    }
                    jsonKhongMax!!.put("soDauG1", jsonSole.toString())
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            update()
        }
        bindDialog.btnXoaDeDauG1.setOnClickListener {
            try {
                jsonKhongMax!!.put("danDauG1", "")
                jsonKhongMax!!.put("soDauG1", JSONObject().toString())
                bindDialog.edtNhapDanDeDauG1.setText("")
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }

        bindDialog.btnKhongDeDitG1.setOnClickListener {
            var str = "ded " + bindDialog.edtNhapDanDeDitG1.text.toString()
            if (str.length > 7) {
                try {
                    str = controller.parseSo(str.convertLatin()).replace("de dit nhat:", "ded:")
                    if (str.contains(KHONG_HIEU)) {
                        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Thêm bị lỗi, hãy sửa lại 1", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            try {
                if (str.length > 7) {
                    jsonKhongMax!!.put("danDitG1", bindDialog.edtNhapDanDeDitG1.text.toString().replace("\n", " "))
                    val jsonSole = JSONObject()
                    while (str.isNotEmpty()) {
                        val line = str.substring(0, str.indexOf("\n") + 1)
                        val noiDung = str.substring(line.indexOf(":") + 1, line.indexOf("\n") + 1)
                        val daySo = noiDung.substring(0, noiDung.indexOf(",x")).split(",")
                        val soTien = line.substring(line.indexOf(",x") + 2, line.indexOf("\n"))

                        daySo.forEach { soChon ->
                            if (!jsonSole.has(soChon) || jsonSole.getInt(soChon) > soTien.toInt()) {
                                jsonSole.put(soChon, soTien)
                            }
                        }

                        str = str.replace(line, "")
                    }
                    jsonKhongMax!!.put("soDitG1", jsonSole.toString())
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            update()
        }
        bindDialog.btnXoaDeDauG1.setOnClickListener {
            try {
                jsonKhongMax!!.put("danDauG1", "")
                jsonKhongMax!!.put("soDauG1", JSONObject().toString())
                bindDialog.edtNhapDanDeDauG1.setText("")
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }

        bindDialog.btnKhongLo.setOnClickListener {
            var str = "de " + bindDialog.edtNhapDanLo.text.toString()
            if (str.length > 7) {
                try {
                    str = controller.parseSo(str.convertLatin()).replace("de dit db:", "de:")
                    if (str.contains(KHONG_HIEU)) {
                        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Thêm bị lỗi, hãy sửa lại", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            try {
                if (str.length > 7) {
                    jsonKhongMax!!.put(
                        "danLo",
                        bindDialog.edtNhapDanLo.text.toString().replace("\n", " ")
                    )
                    val jsonSole = JSONObject()
                    while (str.isNotEmpty()) {
                        val line = str.substring(0, str.indexOf("\n") + 1)
                        val noiDung = str.substring(line.indexOf(":") + 1, line.indexOf("\n") + 1)
                        val daySo = noiDung.substring(0, noiDung.indexOf(",x")).split(",")
                        val soTien = line.substring(line.indexOf(",x") + 2, line.indexOf("\n"))

                        daySo.forEach { soChon ->
                            if (!jsonSole.has(soChon) || jsonSole.getInt(soChon) > soTien.toInt()) {
                                jsonSole.put(soChon, soTien)
                            }
                        }

                        str = str.replace(line, "")
                    }
                    jsonKhongMax!!.put("soLo", jsonSole.toString())
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            update()
        }
        bindDialog.btnXoaLo.setOnClickListener {
            try {
                jsonKhongMax!!.put("danLo", "")
                jsonKhongMax!!.put("soLo", JSONObject().toString())
                bindDialog.edtNhapDanLo.setText("")
            } catch (_: JSONException) {
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }
        bindDialog.btnKhongXienCang.setOnClickListener {
            try {
                jsonKhongMax!!.put("xien2", bindDialog.giuxien2.text.toString())
                jsonKhongMax!!.put("xien3", bindDialog.giuxien3.text.toString())
                jsonKhongMax!!.put("xien4", bindDialog.giuxien4.text.toString())
                jsonKhongMax!!.put("cang", bindDialog.giu3cang.text.toString())
                Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
            } catch (_: JSONException) {
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }
        bindDialog.btnXoaXien.setOnClickListener {
            try {
                jsonKhongMax!!.put("xien2", 0)
                jsonKhongMax!!.put("xien3", 0)
                jsonKhongMax!!.put("xien4", 0)
                jsonKhongMax!!.put("cang", 0)
                bindDialog.giuxien2.setText("0")
                bindDialog.giuxien3.setText("0")
                bindDialog.giuxien4.setText("0")
                bindDialog.giu3cang.setText("0")
            } catch (_: JSONException) {
            } catch (th: Throwable) {
                update()
                throw th
            }
            update()
        }
        dialog.setCancelable(true)
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun update() {
        var danGiuDe: String
        val danGiuLo: String
        try {
            danGiuDe = if (jsonKhongMax!!.getString("danDe").isEmpty())
                "  Đề: Không khống"
            else
                "  Đề: " + jsonKhongMax!!.getString("danDe")

            if (jsonKhongMax!!.getString("danDauDB").isNotEmpty())
                danGiuDe = "$danGiuDe\n  Đầu DB:" + jsonKhongMax!!.getString("danDauDB")

            if (jsonKhongMax!!.getString("danDauG1").isNotEmpty())
                danGiuDe = "$danGiuDe\n  Đầu G1:" + jsonKhongMax!!.getString("danDauG1")

            if (jsonKhongMax!!.getString("danDitG1").isNotEmpty())
                danGiuDe = "$danGiuDe\n  Đít G1:" + jsonKhongMax!!.getString("danDitG1")

            danGiuLo = if (jsonKhongMax!!.getString("danLo").isEmpty())
                " Lô: Không khống"
            else
                " Lô: ${jsonKhongMax!!.getString("danLo")}"

            bind.tvKhongMax.text = """$danGiuDe
            $danGiuLo
            Xiên 2: ${jsonKhongMax!!.getString("xien2")}
            Xiên 3: ${jsonKhongMax!!.getString("xien3")}
            Xiên 4: ${jsonKhongMax!!.getString("xien4")}
            Càng: ${jsonKhongMax!!.getString("cang")}""".trimIndent()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}