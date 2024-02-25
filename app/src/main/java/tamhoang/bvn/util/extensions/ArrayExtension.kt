package tamhoang.bvn.util.extensions

import tamhoang.bvn.data.enum.TL

fun Array<Array<String?>>.getTheLoai(tl: TL?, index: Int): String? {
    return tl?.str ?: this[index - 1][1]
}

operator fun <T> List<T>.component6(): T = get(5)

operator fun <T> List<T>.component7(): T = get(6)

operator fun <T> List<T>.component8(): T = get(7)

operator fun <T> List<T>.component9(): T = get(8)

fun <T> List<T>.combinations(k: Int): List<List<T>> {
    if (k == 0) {
        return listOf(emptyList())
    }

    if (k > this.size) {
        return emptyList()
    }

    if (k == this.size) {
        return listOf(this)
    }

    val result = mutableListOf<List<T>>()

    val subCombinations = this.subList(1, this.size).combinations(k - 1)
    for (subCombination in subCombinations) {
        result.add(listOf(this[0]) + subCombination)
    }

    result.addAll(this.subList(1, this.size).combinations(k))

    return result
}


