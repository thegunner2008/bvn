package tamhoang.bvn.util.extensions

import java.text.DecimalFormat

fun Int.to2ChuSo(): String {
    return if (this < 10) {
        "0$this"
    } else {
        this.toString()
    }
}

fun Int.toDecimal(): String {
    val decimalFormat = DecimalFormat("###,###")
    return decimalFormat.format(this)
}