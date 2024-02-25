package tamhoang.bvn.util.extensions

import java.text.SimpleDateFormat
import java.util.*

fun String.formatDate(format: String = "yyyy-MM-dd", action:((Calendar) -> Unit)? = null): String {
    val sdf = SimpleDateFormat(format)
    val calendar = Calendar.getInstance()
    calendar.time = sdf.parse(this) ?: Date()
    action?.let { it(calendar) }
    return sdf.format(Date(calendar.timeInMillis))
}