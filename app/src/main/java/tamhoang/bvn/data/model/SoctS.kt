package tamhoang.bvn.data.model

import android.content.ContentValues
import android.database.Cursor

class SoctS(
    var id: Int?,
    var ngay_nhan: String,
    var type_kh: Int,
    var ten_kh: String,
    var so_dienthoai: String,
    var so_tin_nhan: Int,
    var the_loai: String,
    var so_chon: String,
    var diem: Double,
    var diem_quydoi: Double,
    var diem_khachgiu: Double,
    var diem_dly_giu: Double,
    var diem_ton: Double,
    var gia: Double,
    var lan_an: Double,
    var so_nhay: Double,
    var tong_tien: Double,
    var ket_qua: Double
) {
    companion object {
        const val TABLE = "tbl_soctS"
        fun toContentValues(soctS: SoctS): ContentValues {
            val values = ContentValues()
            values.put("ID", soctS.id)
            values.put("ngay_nhan", soctS.ngay_nhan)
            values.put("type_kh", soctS.type_kh)
            values.put("ten_kh", soctS.ten_kh)
            values.put("so_dienthoai", soctS.so_dienthoai)
            values.put("so_tin_nhan", soctS.so_tin_nhan)
            values.put("the_loai", soctS.the_loai)
            values.put("so_chon", soctS.so_chon)
            values.put("diem", soctS.diem)
            values.put("diem_quydoi", soctS.diem_quydoi)
            values.put("diem_khachgiu", soctS.diem_khachgiu)
            values.put("diem_dly_giu", soctS.diem_dly_giu)
            values.put("diem_ton", soctS.diem_ton)
            values.put("gia", soctS.gia)
            values.put("lan_an", soctS.lan_an)
            values.put("so_nhay", soctS.so_nhay)
            values.put("tong_tien", soctS.tong_tien)
            values.put("ket_qua", soctS.ket_qua)
            return values
        }

        fun toUpdateValues(
            id: Int? = null,
            ngay_nhan: String? = null,
            type_kh: Int? = null,
            ten_kh: String? = null,
            so_dienthoai: String? = null,
            so_tin_nhan: Int? = null,
            the_loai: String? = null,
            so_chon: String? = null,
            diem: Double? = null,
            diem_quydoi: Double? = null,
            diem_khachgiu: Double? = null,
            diem_dly_giu: Double? = null,
            diem_ton: Double? = null,
            gia: Double? = null,
            lan_an: Double? = null,
            so_nhay: Double? = null,
            tong_tien: Double? = null,
            ket_qua: Double? = null
        ) : ContentValues {
            val values = ContentValues()
            if (id != null) values.put("ID", id)
            if (ngay_nhan != null) values.put("ngay_nhan", ngay_nhan)
            if (type_kh != null) values.put("type_kh", type_kh)
            if (ten_kh != null) values.put("ten_kh", ten_kh)
            if (so_dienthoai != null) values.put("so_dienthoai", so_dienthoai)
            if (so_tin_nhan != null) values.put("so_tin_nhan", so_tin_nhan)
            if (the_loai != null) values.put("the_loai", the_loai)
            if (so_chon != null) values.put("so_chon", so_chon)
            if (diem != null) values.put("diem", diem)
            if (diem_quydoi != null) values.put("diem_quydoi", diem_quydoi)
            if (diem_khachgiu != null) values.put("diem_khachgiu", diem_khachgiu)
            if (diem_khachgiu != null) values.put("diem_khachgiu", diem_khachgiu)
            if (diem_dly_giu != null) values.put("diem_dly_giu", diem_dly_giu)
            if (diem_ton != null) values.put("diem_ton", diem_ton)
            if (gia != null) values.put("gia", gia)
            if (lan_an != null) values.put("lan_an", lan_an)
            if (so_nhay != null) values.put("so_nhay", so_nhay)
            if (tong_tien != null) values.put("tong_tien", tong_tien)
            if (ket_qua != null) values.put("ket_qua", ket_qua)
            return values
        }

        fun parseCursor(cursor: Cursor): SoctS {
            return SoctS(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getInt(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getDouble(8),
                cursor.getDouble(9),
                cursor.getDouble(10),
                cursor.getDouble(11),
                cursor.getDouble(12),
                cursor.getDouble(13),
                cursor.getDouble(14),
                cursor.getDouble(15),
                cursor.getDouble(16),
                cursor.getDouble(17)
            )
        }
    }
}