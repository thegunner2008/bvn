package tamhoang.bvn.ui.mauTinNhan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import tamhoang.bvn.R
import tamhoang.bvn.data.DbOpenHelper
import tamhoang.bvn.databinding.FragSmsTemplatesBinding

class FragSmsTemplates : Fragment() {
    private var _bind: FragSmsTemplatesBinding? = null
    val bind get() = _bind!!

    var db: DbOpenHelper? = null
    var mGiaiThich = ArrayList<String>()
    var mNoiDung = ArrayList<String>()
    override fun onCreateView(layoutInflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View {
        _bind = FragSmsTemplatesBinding.inflate(layoutInflater)
        addtoListview()
        xemLv()
        bind.lvTemplateSms.onItemClickListener = OnItemClickListener { _, _, i, _ ->
            bind.tvMautin.text = mNoiDung[i]
            bind.tvGiaithich.text = mGiaiThich[i]
        }
        return bind.root
    }

    private fun addtoListview() {
        mNoiDung.clear()
        mGiaiThich.clear()
        mNoiDung.add("Viết tắt các dạng:")
        mGiaiThich.add("dea: đề 2 số đầu giải ĐB (đề đầu ĐB/ đầu ĐB)deb: đề 2 số cuối giải đbdet: đề 8 số cuối giải đb nhưng trả thưởng 80.000 (đề 8/ đề ăn 8)dec: đề 2 số đầu giải nhất (đề đầu nhất/ đầu nhất)ded: đề 2 số cuối giải nhất (đề đít nhất/ đít nhất)")
        mNoiDung.add("Đầu đb")
        mGiaiThich.add("2 số đầu giải đặc biệt")
        mNoiDung.add("Đầu nhất")
        mGiaiThich.add("Đề 2 sô đầu giải nhất")
        mNoiDung.add("Đít nhất")
        mGiaiThich.add("Đề 2 số cuối giải nhất")
        mNoiDung.add("Tổng chia 3")
        mGiaiThich.add("Các số chia hết cho 3")
        mNoiDung.add("Chia 3 dư 1")
        mGiaiThich.add("Các số chia cho 3 dư 1")
        mNoiDung.add("Chia 3 dư 2")
        mGiaiThich.add("Các số chia cho 3 dư 2")
        mNoiDung.add("Không chia 3")
        mGiaiThich.add("Các số không chia hết cho 3")
        mNoiDung.add("Tổng trên 10")
        mGiaiThich.add("Các số có tổng lớn hơn 10")
        mNoiDung.add("Tổng dưới 10")
        mGiaiThich.add("Các số có tổng bé hơn 10")
        mNoiDung.add("Tổng 10")
        mGiaiThich.add("Phần mềm sẽ báo lỗi vì không có tổng 10, chỉ có tổng 0 hoặc tổng 1 và 0 thì ghi tổng 01")
        mNoiDung.add("xg2 010,030,78,89,60 x 10")
        mGiaiThich.add("Phần mềm sẽ tự động ghép xiên 2 của tất cả các số với nhau, hãy kiểm tra cẩn thận có số giống nhau khi phần mềm báo lỗi")
        mNoiDung.add("xg3 010,030,78,89,60 x 10")
        mGiaiThich.add("Phần mềm sẽ tự động ghép xiên 3 của tất cả các số với nhau, hãy kiểm tra cẩn thận có số giống nhau khi phần mềm báo lỗi")
        mNoiDung.add("xg4 010,030,78,89,60 x 10")
        mGiaiThich.add("Phần mềm sẽ tự động ghép xiên 4 của tất cả các số với nhau, hãy kiểm tra cẩn thận có số giống nhau khi phần mềm báo lỗi")
        mNoiDung.add("De dan 18 bor kep x 10")
        mGiaiThich.add("Chữ bo có 2 nghĩa là bỏ và bộ nên chữ bỏ phải thêm chữ 'r' thành bor")
        mNoiDung.add("De boj 02,04 x 10")
        mGiaiThich.add("Chữ bo có 2 nghĩa là bỏ và bộ nên chữ bộ phải thêm chữ 'j' thành boj")
        mNoiDung.add("de giap ty x 100, de giap chuột x 100")
        mGiaiThich.add("Các con giáp sẽ được ghi bằng cách viết giap + tên con giáp")
    }

    private fun xemLv() {
        bind.lvTemplateSms.adapter = TNGAdapter(
            activity,
            R.layout.frag_sms_temp_lv,
            mNoiDung
        )
    }

    internal inner class TNGAdapter(context: Context?, i: Int, list: List<String?>?) : ArrayAdapter<Any?>(
        context!!, i, list!!
    ) {
        @SuppressLint("ViewHolder")
        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val inflate = (context as Activity).layoutInflater.inflate(R.layout.frag_sms_temp_lv, null as ViewGroup?)
            (inflate.findViewById<View>(R.id.tv_noidung) as TextView).text = mNoiDung[i]
            return inflate
        }
    }
}