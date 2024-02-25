package tamhoang.bvn.util.extensions

import android.content.ContentValues
import android.util.Log
import tamhoang.bvn.constants.Const
import java.util.*

fun String?.isNumeric(): Boolean {
    return this?.matches(Regex("[0-9]+")) ?: false
}

fun String.clear(char: String): String {
    return this.replace(char, "")
}

fun String.clear(characters: List<String>): String {
    var result = this
    characters.forEach { character ->
        result = result.replace(character, "")
    }
    return result
}

fun String.clearDuplicate(char: Char = ' ', time: Int = 1): String {
    var result = this
    repeat(time) {
        result = result.replace("$char$char", "$char")
    }
    return result
}

fun String.replaceMap(map: Map<String, String>): String {
    var result = this
    map.forEach { (key, value) ->
        result = result.replace(key, value)
    }
    return result
}


fun String.contains(strings: List<String>): Boolean {
    return strings.any { string ->
        this.contains(string)
    }
}

fun String.startsWith(prefixes: List<String>): Boolean {
    prefixes.forEach { prefix ->
        if (this.startsWith(prefix)) {
            return true
        }
    }
    return false
}

fun String.between(start: String, end: String): String {
    val startIndex = this.indexOf(start) + start.length + 1
    val endIndex = this.indexOf(end)
    return this.substring(startIndex, endIndex)
}

fun String.between(start: Int, end: String): String {
    val endIndex = this.indexOf(end)
    return this.substring(start, endIndex)
}

fun String.between(start: String, end: Int? = null): String {
    val startIndex = this.indexOf(start) + start.length + 1
    return if (end != null) this.substring(startIndex, end) else this.substring(startIndex)
}

fun String.countIndex(
    startIndex: Int = 0,
    endIndex: Int = this.length - 1,
    action: (Char) -> Boolean
): Int {
    if (startIndex < 0 || endIndex >= this.length) return startIndex
    for (i in startIndex..endIndex) {
        if (!action(this[i])) {
            return i
        }
    }
    return endIndex
}

fun String.indexOfs(find: String): List<Int> {
    val list = mutableListOf<Int>()
    var index = 0
    while (index >= 0) {
        list.add(index)
        index = this.indexOf(find, index + 1)
    }
    return list
}

fun String.indexOfFulls(find: String, action: (Int, Int) -> Unit) {
    var index = 0
    while (index >= 0) {
        val start = if (index == 0) 0 else index + 1
        index = this.indexOf(find, index + 1)
        action(start, index)
    }
}

fun String.countTrueIndex(
    startIndex: Int = 0,
    endIndex: Int = this.length - 1,
    action: (Char) -> Boolean
): Int {
    if (startIndex < 0 || endIndex >= this.length) return startIndex
    for (i in startIndex..endIndex) {
        if (!action(this[i])) {
            return i - 1
        }
    }
    return endIndex
}

fun String.countTrueIndex(
    startIndex: Int = 0,
    endIndex: Int = this.length - 1,
    action: (Char) -> Unit,
    condition: (Char) -> Boolean
): Int {
    if (startIndex < 0 || endIndex >= this.length) return startIndex
    for (i in startIndex..endIndex) {
        if (!condition(this[i])) {
            return i
        }
        action(this[i])
    }
    return endIndex
}

fun String.countDownIndex(
    startIndex: Int = 0,
    endIndex: Int = this.length - 1,
    action: (Char) -> Boolean
): Int {
    if (startIndex < 0 || endIndex >= this.length) return endIndex
    for (i in endIndex downTo startIndex) {
        if (!action(this[i])) {
            return i
        }
    }
    return startIndex
}

fun String.insert(index: Int, string: String): String {
    return this.substring(0, index) + string + this.substring(index, this.length)
}

fun String.highlightError(value: String): String {
    val highlight = "${Const.LDPRO}$value${Const.FONT}"
    return if (this.contains(value)) {
        this.replace(value, highlight)
    } else "${Const.LDPRO}$this${Const.FONT}"
}

fun String.convertLatin(): String {
    var str = this.toLowerCase(Locale.ROOT) + " "

    mapOf(
        "bỏ" to "bor",
        "bộ" to "boj",
        "." to ",",
        "́" to "",
        "̀" to "",
        "̃" to "",
        "̉" to "",
        "̣" to "",
        "̃" to "",
        "+" to "!",
        "\n d" to "\nd"
    )
        .forEach { (s1, s2) -> str = str.replace(s1, s2) }

    val origin = "ăâàằầáắấảẳẩãẵẫạặậễẽểẻéêèềếẹệôòồơờóốớỏổởõỗỡọộợưúùứừủửũữụựìíỉĩịỳýỷỹỵđ×"
    val convert = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeooooooooooooooooouuuuuuuuuuuiiiiiyyyyydx"
    for (i in origin.indices) {
        str = str.replace(origin[i], convert[i])
    }

    str = str.replace("\n d", "\nd")

    if (str.contains("\nd")) {
        val splits = str.split("\nd").filter { it.isNotEmpty() }
        splits.forEach {
            val char = it[0]
            if (char.isDigit() || char.isWhitespace() || char == ':') {
                str = str.replace("\nd$char", "!d$char")
            }
        }
    }

    str = str.clearDuplicate(time = 9)

    mapOf(
        "d e" to "de",
        "d au" to "dau",
        "d it" to "dit",
        "ja" to "ia",
        "dich" to "dit",
        "je" to "ie",
        "nde" to "n de",
        "nlo" to "n lo",
        "nxi" to "n xi",
        "nda" to "n da",
        "ndi" to "n di",
        "nto" to "n to",
        "x i" to "xi",
        "x j" to "xi",
        "xj" to "xi",
        "x 3 bc" to "x 3, bc",
        "x 3\nbc" to "x 3, bc"
    )
        .forEach { (s1, s2) -> str = str.replace(s1, s2) }

    Log.e(ContentValues.TAG, "convertLatin: output $str" )

    return str
}