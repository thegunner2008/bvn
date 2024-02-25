package tamhoang.bvn.ui.khachHang

import tamhoang.bvn.constants.Const
import tamhoang.bvn.data.enum.TL
import tamhoang.bvn.data.services.xuLyTinNhan.MappingTin
import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.theLoai.LoDe
import tamhoang.bvn.util.extensions.*
import tamhoang.bvn.util.ld.CongThuc
import kotlin.math.min

class KhachHangController {
    fun parseSo(str: String): String {
        var conlai2: String? = ""
        var dayso = ""
        var bieuthuc: String

        var ndPhanTich = MappingTin.fromJsonFile(
            CongThuc.fixDanSo(
                str.replace("\n", " ").replace(".", ",").convertLatin()
            )
        )

        ndPhanTich = MappingTin.det(ndPhanTich)
        if (ndPhanTich.length < 3 || ndPhanTich.length < 8) {
            return "${Const.KHONG_HIEU} $ndPhanTich"
        }
        ndPhanTich = MappingTin.xuLyDauX(ndPhanTich)
        if (!ndPhanTich.contains("x ") && !ndPhanTich.contains(Const.KHONG_HIEU)) {
            return "${Const.KHONG_HIEU}  $ndPhanTich"
        }

        if (ndPhanTich.contains(Const.KHONG_HIEU)) {
            return ndPhanTich
        }
        ndPhanTich = ndPhanTich.replace(" , ", " ")
            .clearDuplicate(time = 9)
            .trim() + " "

        var conlai: String? = null
        var daysoUnknow = ""
        var k = 0
        var iX = -1
        var index = 0

        val mang = Array(400) { arrayOfNulls<String>(6) }

        while (true) {
            val indexOfX = ndPhanTich.indexOf(" x ", iX + 1)
            iX = indexOfX
            if (indexOfX == -1) {
                conlai2 = conlai
                dayso = ""
                break
            }

            val soTien = StringBuilder()
            val iStartTien = ndPhanTich.countIndex(startIndex = iX) {
                if ("0123456789,tr".contains(it)) {
                    soTien.append(it)
                }
                !it.isWhitespace() || soTien.isEmpty()
            }

            val dtien = StringBuilder()
            var iEnd = ndPhanTich.countIndex(startIndex = iStartTien) {
                dtien.append(it)
                it.isLetter() || dtien.isEmpty()
            }

            iEnd = min(iEnd + 1, ndPhanTich.length - 1)
            val keyWords = arrayOf(
                "dau",
                "dit",
                "tong",
                "cham",
                "dan",
                "boj",
                "lo",
                "de",
                "xi",
                "xn",
                "hc",
                "xq",
                "xg",
                "bc",
                "kep",
                "sat",
                "to",
                "nho",
                "chan",
                "le",
                "ko",
                "chia",
                "duoi",
                "be"
            )

            if (keyWords.none { dtien.contains(it) }) {
                if (dtien.contains("x ")) {
                    val iCount = ndPhanTich.countDownIndex(endIndex = iStartTien - 1) {
                        it.isDigit()
                    }

                    if (iCount <= 0) {
                        bieuthuc = daysoUnknow
                    } else {
                        bieuthuc = ndPhanTich.substring(k, iCount + 1)
                        conlai = ndPhanTich.substring(iCount + 1)
                        k = iCount + 1
                    }
                } else {
                    val bieuThucCut = ndPhanTich.substring(k, iEnd)

                    bieuthuc = if (bieuThucCut.substring(0, 4).contains("bor")) {
                        "de $bieuThucCut"
                    } else {
                        bieuThucCut
                    }
                    k = iEnd
                    conlai = ndPhanTich.substring(iEnd)
                }

                val theLoai = TL.getTL(bieuthuc)

                mang[index][0] = bieuthuc
                mang[index][1] = mang.getTheLoai(theLoai, index = index)
                val dso = if (theLoai != TL.NULL)
                    bieuthuc.clear(listOf("${theLoai.t}:", "${theLoai.t} :", theLoai.t))
                else
                    bieuthuc
                mang[index][2] = LoDe.parse(dso.substring(0, dso.indexOf("x")))
                mang[index][3] = theLoai.getTien(bieuthuc)
                if (mang[index][2]!!.contains(Const.KHONG_HIEU)) {
                    if (mang[index][2]!!.length < 3) {
                        break
                    } else if (mang[index][3]!!.contains(Const.KHONG_HIEU)) {
                        conlai2 = conlai
                        dayso = "${Const.KHONG_HIEU} " + mang[index][2]!!.substring(mang[index][2]!!.indexOf("x"))
                        break
                    } else {
                        daysoUnknow = mang[index][2]!!
                    }
                }
                index++
            }
        }
        val replace = conlai2!!.clear(listOf(" ", ".", ","))
        if (replace.isNotEmpty()) {
            return "${Const.KHONG_HIEU} $conlai2"
        }
        if (dayso.contains("${Const.KHONG_HIEU} ")) {
            return dayso
        }
        val result =
            mang.filter { it[0] != null && !it[2]!!.contains("${Const.KHONG_HIEU} ") && !it[3]!!.contains("${Const.KHONG_HIEU} ") }
                .joinToString("") {
                    "${it[1]}:${it[2]}x${it[3]}\n"
                }
        return result
    }
}