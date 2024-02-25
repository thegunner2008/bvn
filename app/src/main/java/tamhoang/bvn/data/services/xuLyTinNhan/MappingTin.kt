package tamhoang.bvn.data.services.xuLyTinNhan

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.map.MapKyTu
import tamhoang.bvn.data.store.KhachHangStore
import tamhoang.bvn.ui.main.MainState
import tamhoang.bvn.util.extensions.*
import tamhoang.bvn.util.ld.CongThuc
import kotlin.math.max

object MappingTin {
    fun fromMapKytu(tinNhan: String) = tinNhan
            .replaceMap(MapKyTu.kt1)
            .replaceMap(MapKyTu.kt2)
            .replaceMap(MapKyTu.kt3)

    fun fromJsonFile(tinNhan: String): String {
        var result = tinNhan

        MainState.formList.forEach {
            result = result.replace(it["datas"]!!, it["type"].toString())
                .replace("  ", " ")
        }

        MainState.formArray.forEach {
            result = result.replace(it["str"]!!, it["repl_str"].toString())
                .replace("  ", " ")
        }

        return result.clearDuplicate(time = 9)
    }

    fun det(tinNhan: String, tenKh: String? = null): String {
        val deBangDe8 =
            if (tenKh != null)
                KhachHangStore.I.getSettingKHByName(tenKh).khachDe.value() == 1
            else false
        return if (!tinNhan.contains(
                listOf(
                    "dea", "deb", "dec",
                    "ded", "det", "de"
                )
            )
        )
            tinNhan.replace("de", if (deBangDe8) "det " else "deb ")
        else tinNhan
    }

    // Thêm bớt dấu "x" vào tin nhắn
    fun xuLyDauX(str: String): String {
        if (str.contains(Const.KHONG_HIEU)) return str

        var str1 = str.clearDuplicate() + "      "
        var i = 3
        while (i < str1.length - 4) {
            // insert x vào nếu có format "[số] [n/d/k] "
            if (str1[i].isDigit() && str1[i + 1].isWhitespace() && "ndk".contains(str1[i + 2]) && str1[i + 3].isWhitespace()) {
                val index = str1.countDownIndex(endIndex = i) { it.isDigit() }
                str1 = str1.insert(max(0, index), " x ")
                i += 6
            }
            i++
        }
        str1 = str1.clearDuplicate(time = 6)
        repeat(3) {
            str1 = str1
                .replace("x x", "x")
                .replace("xx", "x")
                .replace(": x", " x")
                .replace(":x", " x")
                .replace(", x", " x")
                .replace(",x", " x")
                .replace("-x", " x")
                .replace("- x", " x")
        }
        return str1
    }

    // Xử lý "Ok tin" đầu tin nhắn
    fun removeOkTin(ndPhanTich: String): String {
        var str = ndPhanTich
        if (str.startsWith("tin")) str.replaceFirst("tin", Const.T)
        str = CongThuc.fixTinNhan(ndPhanTich) + " "
        var i1 = -1
        while (true) { // "abc tin 123456 def" => "abc def"
            val indexTin = str.indexOf("tin", i1 + 1)
            i1 = indexTin // vi tri cua "tin" trong nd_phantich
            if (indexTin == -1) {
                break
            }
            var e = i1 + 5 //i1 + 3 là vi tri ket thuc cua tin trong nd_phantich
            while (e < i1 + 10 && CongThuc.isNumeric(str.substring(i1 + 4, e))
            ) { // sau chu "tin" kiem tra 6 chu tiep theo deu là so thi dung lai
                e++
            }
            if (e > i1 + 5) {
                str = str.substring(0, i1) + str.substring(e)
            }
        }
        str = str.trim()
        for (i in 6 downTo 1) { // nd_phantich: "T 123 abc" => " abc"
            val ss = ndPhanTich.substring(0, i)
            val clear = ss.clear(listOf(Const.T, " ", ","))
            if (ss.trim().contains(Const.T) && CongThuc.isNumeric(clear)) {
                str = str.substring(i)
            }
        }
        return str
    }
}