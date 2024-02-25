package tamhoang.bvn.data.enum

import tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.SoTien
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.extensions.contains

enum class TL(val t: String, val str: String) {
    LoA("loa", "lo dau"),
    Lo("lo", "lo"),
    DeA("dea", "de dau db"),
    DeB("deb", "de dit db"),
    DeC("dec", "de dau nhat"),
    DeD("ded", "de dit nhat"),
    Det("det", "de 8"),
    De("de", "de dit db"),
    HC("hc", "hai cua"),
    XN("xn", "xn"),
    BCA("bca", "bc dau"),
    BC("bc", "bc"),
    XIA("xia", "xien dau"),
    XI("xi", "xi"),
    XG2("xg 2", "xg 2"),
    XG3("xg 3", "xg 3"),
    XG4("xg 4", "xg 4"),
    XQA("xqa", "xqa"),
    XQ("xq", "xq"),
    NULL("", "");

    companion object {
        fun getTL(str: String, de8: Boolean = false): TL {
            return when {
                str.contains("loa") -> LoA
                str.contains("lo") -> Lo
                str.contains("dea") -> DeA
                str.contains("deb") -> DeB
                str.contains("dec") -> DeC
                str.contains("ded") -> DeD
                str.contains("de ") -> if (de8) Det else DeB
                str.contains("det") -> Det
                str.contains("hc") -> HC
                str.contains("xn") -> XN
                str.contains("bca") -> BCA
                str.contains(listOf("bc", "cang")) -> BC
                str.contains("xia") -> XIA
                str.contains("xi") -> XI
                str.contains("xg 2") -> XG2
                str.contains("xg 3") -> XG3
                str.contains("xg 4") -> XG4
                str.contains("xqa") -> XQA
                str.contains("xq") -> XQ
                else -> NULL
            }
        }

        fun getTLToInsertSoct(str: String): TL {
            listOf(LoA, Lo, DeA, DeB, DeC, DeD, Det, BCA, BC, XIA, XN).forEach {
                if (str.contains(it.str)) return it
            }
            listOf(XI, XG2, XG3, XG4).forEach {
                if (str.contains(it.str)) return XI
            }
            return NULL
        }
    }

    fun isLo() = (this == Lo || this == LoA)

    fun isDe() = listOf(DeA, DeB, DeC, DeD, Det, HC).contains(this)

    fun isBaCang() = (this == BCA || this == BC)

    fun isXien() = listOf(XIA, XI, XG2, XG3, XG4, XQA, XQ, XN).contains(this)

    fun isXienQuay() = listOf(XQ, XQA).contains(this)

    fun isNhat() = listOf(DeC, DeD, HC).contains(this)

    fun getTien(noidung: String): String {
        val dayso = if (this != NULL) {
            noidung.clear(listOf("$t:", "$t :", t))
        } else {
            noidung
        }

        return SoTien.parse(dayso.substring(dayso.indexOf("x"), dayso.length), NULL)
    }
}