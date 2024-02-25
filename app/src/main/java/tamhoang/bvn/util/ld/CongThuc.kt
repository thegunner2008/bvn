package tamhoang.bvn.util.ld

import android.util.Log
import org.json.JSONException
import tamhoang.bvn.constants.Const.KHONG_HIEU
import tamhoang.bvn.data.setting.Setting
import tamhoang.bvn.util.extensions.clearDuplicate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object CongThuc {
    @JvmStatic
    fun fixDanSo(str: String): String {
        var str2 = str.replace(" ,", ", ")
        var i = 0
        var j = str2.length
        while (i < j - 1) {
            i++
            j = str2.length - 1
            if (Character.isLetter(str2[i]) && !Character.isLetter(str2[i + 1])) {
                str2 = str2.substring(0, i + 1) + " " + str2.substring(i + 1)
                i++
            } else if (!Character.isLetter(str2[i]) && Character.isLetter(str2[i + 1])) {
                str2 = str2.substring(0, i + 1) + " " + str2.substring(i + 1)
                i++
            }
        }
        var str3 = "$str2 ".clearDuplicate(time = 9)
        if (str3.contains("(") && str3.contains(")")) {
            var i1 = -1
            while (true) {
                val indexOf = str3.indexOf("(", i1 + 1)
                i1 = indexOf
                if (indexOf == -1) {
                    break
                }
                var i22 = i1
                while (i22 < str3.length && !str3.substring(i22, i22 + 1).contains(")")) {
                    i22++
                }
                if (isNumeric(str3.substring(i1 + 1, i22).replace(" ", ""))) {
                    for (i3 in i1 until i22) {
                        Log.i("ISNUMBERIC", i1.toString() + "")
                        if (isNumeric(str3.substring(i3 - 1, i3)) && str3.substring(i3, i3 + 1)
                                .contains(" ") && isNumeric(str3.substring(i3 + 1, i3 + 2))
                        ) {
                            str3 = str3.substring(0, i3) + "," + str3.substring(i3 + 1)
                        }
                    }
                }
            }
        }
        return str3
    }

    @JvmStatic
    fun fixTinNhan(str: String): String {
        var str2 = "$str "
        str2.replace(" ", ",").replace(".", ",").replace(":", ",")
            .replace(";", ",").replace("/", ",").split(",")

        var i = -1
        if (str2.contains(KHONG_HIEU)) {
            return str2
        }
        for (i2 in str2.indices) {
            if (str2[i2].toInt() > 127 || str2[i2].toInt() < 31) {
                str2 = str2.substring(0, i2) + " " + str2.substring(i2 + 1)
            }
        }
        var str3 = str2.trim { it <= ' ' }
        if (str3[str3.length - 1] == 'x') {
            str3 = str3.substring(0, str3.length - 1)
        }
        var dem = -1
        while (true) {
            val indexOf = str3.indexOf("x ", dem + 1)
            dem = indexOf
            if (indexOf == -1) {
                break
            }
            var i3 = dem + 2
            while (i3 < str3.length && !isNumeric(str3.substring(i3, i3 + 1))) {
                i3++
            }
            var j = i3
            while (j < str3.length && (isNumeric(str3[j].toString() + "") || " tr".contains(
                    str3[j].toString() + ""
                ))
            ) {
                j++
            }
            if (isNumeric(str3.substring(dem + 1, j).trim { it <= ' ' }) && str3.substring(
                    dem + 1,
                    j
                ).trim { it <= ' ' }.length > 1
            ) {
                str3 = str3.substring(0, j) + " " + str3.substring(j)
            } else if (j - i3 > 1 && str3.substring(dem)
                    .indexOf("to") != j - dem - 1 && str3.substring(dem)
                    .indexOf("tin") != j - dem - 1 && str3.substring(dem).indexOf(",") != j - dem
            ) {
                str3 = str3.substring(0, j) + " " + str3.substring(j)
            } else if (j - i3 == 1 && !str3.substring(dem).contains("tr")) {
                str3 = str3.substring(0, j) + " " + str3.substring(j)
            }
        }
        var str4 = "$str3 "
        var dem2 = str4.length
        while (dem2 > str4.length - 9) {
            val Sss = str4.substring(dem2)
            if (Sss.trim { it <= ' ' }.indexOf("t ") > i) {
                var Sss1 = ""
                for (i4 in dem2 downTo 1) {
                    Sss1 = str4.substring(i4, dem2)
                    if (!isNumeric(Sss1) && Sss1.trim().isNotEmpty()) {
                        break
                    }
                }
                if (Sss1.trim { it <= ' ' }.length > 1 || !isNumeric(Sss1)) {
                    val Sss2 = Sss.replace("t", "").replace(" ", "")
                        .replace(",", "")
                    str4 = if (!isNumeric(Sss2) || Sss2.toInt() >= 99) {
                        if (Sss2.isNotEmpty()) {
                            break
                        }
                        str4.substring(0, dem2 + 1) + "?"
                    } else {
                        str4.substring(0, dem2)
                    }
                }
            }
            dem2--
            i = -1
        }
        var str5 = str4.trim { it <= ' ' }
        try {
            if (str5.substring(str5.length - 1).indexOf("@") > -1) {
                var i5 = str5.length - 2
                while (true) {
                    if (i5 <= 0) {
                        break
                    } else if (str5.substring(i5, i5 + 1).indexOf("@") > -1) {
                        break
                    } else {
                        i5--
                    }
                }
                if (i5 > str5.length - 13 && isNumeric(
                        str5.substring(i5).replace("@", "")
                    )
                ) {
                    str5 = str5.substring(0, i5)
                }
            }
        } catch (e: Exception) {
        }
        try {
            if (!Setting.I.baoTinThieu.isTrue()) {
                str5 = str5.trim { it <= ' ' }
                for (i6 in 6 downTo 1) {
                    val Sss3 = str5.substring(0, i6)
                    if (Sss3.trim { it <= ' ' }.contains("t") && isNumeric(
                            Sss3.replace("t", "").replace(",", "")
                        )
                    ) {
                        str5 = str5.substring(i6)
                    }
                }
            }
        } catch (e2: JSONException) {
            e2.printStackTrace()
        }
        var str6 = "$str5 "
        var i1 = -1
        while (true) {
            try {
                val indexOf2 = str6.indexOf("tin", i1 + 1)
                i1 = indexOf2
                if (indexOf2 == -1) {
                    break
                }
                var i7 = i1 + 5
                while (true) {
                    if (i7 >= i1 + 10) {
                        break
                    } else if (!isNumeric(str6.substring(i1 + 4, i7))) {
                        break
                    } else {
                        i7++
                    }
                }
                if (i7 - i1 > 5) {
                    str6 = str6.substring(0, i1) + str6.substring(i7)
                }
            } catch (e3: Exception) {
            }
        }
        val str7 = str6.trim { it <= ' ' }
        return if (str7.substring(0, 1).indexOf(",") > -1) {
            str7.substring(1)
        } else str7
    }

    fun tachXien(s: String): String? { //"121,232" -> "12,21,23,32"
        if (s.length == 3 && isNumeric(s)) {
            val so1 = s[0]
            val so2 = s[1]
            val so3 = s[2]
            return if (so1 != so2 && so1 == so3)
                "$so1$so2,$so2$so1"
            else
                null
        } else if ((s.length != 2 || !isNumeric(s)) && s.length > 1) {
            return null
        }
        return ""
    }

    @JvmStatic
    fun sortXien(xien: String): String {
        val numberList = xien.split(",").filter { it.isNotEmpty() }.toMutableList()
        numberList.sort()
        return numberList.joinToString(",")
    }

    @JvmStatic
    fun isNumeric(str: String?): Boolean {
        return str?.matches(Regex("[0-9]+")) ?: false
    }

    fun isNumericComma(str: String?): Boolean { // kiem tra có phai la day so khong ( remove ',')
        val check = str?.replace(",", "")?.trim()
        return !check.isNullOrBlank() && check.matches(Regex("[0-9]+"))
    }

    private fun parseDate(date: String): Date {
        return try {
            SimpleDateFormat("HH:mm", Locale.US).parse(date)
        } catch (e: ParseException) {
            Date(0)
        }
    }

    @JvmStatic
    fun checkTime(time: String): Boolean { // da qua Time chua?
        val gioKT = parseDate(time)
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        // return false
        return parseDate("$hour:$minute").after(gioKT)
    }

    @JvmStatic
    fun checkDate(time: String?): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val calendar = Calendar.getInstance()
        return try {
            calendar.time = sdf.parse(time ?: "01/01/2020") ?: Date()
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Date().before(sdf.parse(sdf.format(calendar.time)))
        } catch (e: ParseException) {
            e.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun checkIsToday(date: Date): Boolean {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }

        return now.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH) &&
                now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    }
}