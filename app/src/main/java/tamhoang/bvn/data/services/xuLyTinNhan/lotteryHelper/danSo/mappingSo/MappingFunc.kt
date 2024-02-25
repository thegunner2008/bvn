package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

import tamhoang.bvn.constants.Const
import tamhoang.bvn.util.extensions.clear
import tamhoang.bvn.util.extensions.to2ChuSo
import tamhoang.bvn.util.ld.CongThuc

object MappingFunc {
    fun layDau(str: String): String { // lay dau (co the là 1 day so vd: '12' '1,2')
        val arr = arrayOf(
            "00,01,02,03,04,05,06,07,08,09,",
            "10,11,12,13,14,15,16,17,18,19,",
            "20,21,22,23,24,25,26,27,28,29,",
            "30,31,32,33,34,35,36,37,38,39,",
            "40,41,42,43,44,45,46,47,48,49,",
            "50,51,52,53,54,55,56,57,58,59,",
            "60,61,62,63,64,65,66,67,68,69,",
            "70,71,72,73,74,75,76,77,78,79,",
            "80,81,82,83,84,85,86,87,88,89,",
            "90,91,92,93,94,95,96,97,98,99,"
        )
        if (!CongThuc.isNumericComma(str)) {
            return "${Const.KHONG_HIEU} $str"
        }
        return str.filter { it.isDigit() }.map { arr[Character.getNumericValue(it)] }
            .joinToString(separator = "")
    }

    fun layDit(str: String): String { // lay dit (co the là 1 day so vd: '12' '1,2')
        val arr = arrayOf(
            "00,10,20,30,40,50,60,70,80,90,",
            "01,11,21,31,41,51,61,71,81,91,",
            "02,12,22,32,42,52,62,72,82,92,",
            "03,13,23,33,43,53,63,73,83,93,",
            "04,14,24,34,44,54,64,74,84,94,",
            "05,15,25,35,45,55,65,75,85,95,",
            "06,16,26,36,46,56,66,76,86,96,",
            "07,17,27,37,47,57,67,77,87,97,",
            "08,18,28,38,48,58,68,78,88,98,",
            "09,19,29,39,49,59,69,79,89,99,"
        )
        if (!CongThuc.isNumericComma(str)) {
            return "${Const.KHONG_HIEU} $str"
        }
        return str.filter { it.isDigit() }.map { arr[Character.getNumericValue(it)] }
            .joinToString(separator = "")
    }

    fun lay100so(dieuKien: ((String) -> Boolean)? = null): String {
        val sb = StringBuilder()
        for (i in 0..99) {
            val numStr = i.to2ChuSo()
            val check = if (dieuKien != null) dieuKien(numStr) else true
            if (check) {
                sb.append("$numStr,")
            }
        }
        return sb.toString()
    }

    fun layTong(str: String): String { // lay tong (co the là 1 day so vd: '12' '1,2')
        val arr = arrayOf(
            "00,19,28,37,46,55,64,73,82,91,",
            "01,10,29,38,47,56,65,74,83,92,",
            "02,11,20,39,48,57,66,75,84,93,",
            "03,12,21,30,49,58,67,76,85,94,",
            "04,13,22,31,40,59,68,77,86,95,",
            "05,14,23,32,41,50,69,78,87,96,",
            "06,15,24,33,42,51,60,79,88,97,",
            "07,16,25,34,43,52,61,70,89,98,",
            "08,17,26,35,44,53,62,71,80,99,",
            "09,18,27,36,45,54,63,72,81,90,"
        )
        if (!CongThuc.isNumericComma(str)) {
            return "${Const.KHONG_HIEU} $str"
        }
        return str.filter { it.isDigit() }.map { arr[Character.getNumericValue(it)] }
            .joinToString(separator = "")
    }

    fun layBo(str: String): String {
        val arr = arrayOf(
            "00,050,55",
            "010,060,515,565",
            "020,070,525,575",
            "030,080,535,585",
            "040,090,545,595",
            "11,66,161",
            "121,171,626,676",
            "131,181,636,686",
            "141,191,646,696",
            "22,77,272",
            "232,282,737,787",
            "242,292,747,797",
            "33,88,383",
            "343,393,848,898",
            "44,494,99"
        )
        val sb = StringBuilder()
        for (i in 0 until str.length - 1) {
            val doi = str.substring(i, i + 2)
            if (CongThuc.isNumeric(doi)) {
                val matchingBo = arr.find { s -> s.contains(doi) }
                sb.append("$matchingBo,")
            }
        }
        return fixDan(sb.toString())
    }

    private fun fixDan(str: String): String {
        val sb = StringBuilder()
        val array = str.clear(listOf(":", " . ", " , ")).replace(".", ",")
            .split(",").filter { it.isNotEmpty() }
        for (item in array) {
            if (CongThuc.isNumeric(item) && item.length == 2) {
                sb.append("$item,")
            } else if (CongThuc.isNumeric(item) && item.length == 3) {
                if (item[0] != item[2]) {
                    return "${Const.KHONG_HIEU} $item"
                }
                sb.append(item.substring(0, 2) + "," + item.substring(1, 3) + ",")
            } else if (item.isNotEmpty()) {
                return "${Const.KHONG_HIEU} $item"
            }
        }
        return sb.toString()
    }
}