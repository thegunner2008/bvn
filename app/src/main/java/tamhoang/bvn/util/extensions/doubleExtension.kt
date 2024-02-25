package tamhoang.bvn.util.extensions

import java.text.DecimalFormat

fun Double.toDecimal(): String {
    val decimalFormat = DecimalFormat("###,###")
    return decimalFormat.format(this)
}