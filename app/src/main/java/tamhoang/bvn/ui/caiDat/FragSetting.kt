package tamhoang.bvn.ui.caiDat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.databinding.FragSettingBinding
import tamhoang.bvn.ui.main.MainActivity

class FragSetting : Fragment() {
    private var _bind: FragSettingBinding? = null
    val bind get() = _bind!!

    var db: DbOpenHelper? = null

    private fun createUI(spinner: Spinner, modelSetting: Setting.Model, action: (() -> Unit)? = null) {
        spinner.adapter = ArrayAdapter(activity!!, R.layout.spinner_item, modelSetting.list)
        spinner.setSelection(modelSetting.value())
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                modelSetting.save(position)
                action?.invoke()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bind = FragSettingBinding.inflate(inflater, container, false)
        db = DbOpenHelper(activity!!)

        createUI(bind.spBCApman, Setting.I.apMan)
        createUI(bind.spHetgio, Setting.I.tinQuaGio)
        createUI(bind.spBoTintrung, Setting.I.nhanTinTrung)
        createUI(bind.spKytu, Setting.I.gioiHanTin)
        createUI(bind.spSapxepbaocao, Setting.I.baoCaoSo)
        createUI(bind.spChuyenXien, Setting.I.chuyenXien)
        createUI(bind.spLamtron, Setting.I.lamTron)
        createUI(bind.spLuachonBC, Setting.I.kieuBaoCao) {
            MainActivity.refreshFragBaoCao()
        }
        createUI(bind.spTrathuonglo, Setting.I.traThuongLo)
        createUI(bind.spCanhbao, Setting.I.canhBaoDonVi)
        createUI(bind.spChotTachxien234, Setting.I.tachXienTinChot)
        createUI(bind.spBaotinthieu, Setting.I.baoTinThieu)

        return bind.root
    }
}