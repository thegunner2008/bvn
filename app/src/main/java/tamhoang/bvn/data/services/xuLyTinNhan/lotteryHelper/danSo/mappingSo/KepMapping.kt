package tamhoang.bvn.data.services.xuLyTinNhan.lotteryHelper.danSo.mappingSo

class KepMapping: MappingNumber() {
    override fun condition(str: String) = str.contains("kep")

    override fun convert(originMsg: String, currentMsg: String, index: Int): String {
        var str = currentMsg.replace("bang", "")
        var i121 = -1
        while (true) {
            val indexOf6 = str.indexOf("kep", i121 + 1)
            i121 = indexOf6
            if (indexOf6 == -1) {
                break
            } else if (!str.contains("sat") || str.indexOf("sat") >= i121 || str.indexOf(
                    "lech"
                ) >= i121 + 6 || str.indexOf("lech") <= -1
            ) {
                if (str.indexOf("sat") < i121 && str.contains("sat")) {
                    val sss5 = str.substring(str.indexOf("sat"), i121 + 3)
                        .replace(" ", "").replace(",", "")
                    if (sss5.contains("satkep")) {
                        str = str.substring(
                            0,
                            str.indexOf("sat")
                        ) + " " + str.substring(i121 + 3, str.length)
                        i121 = 0
                        result += "01,10,12,21,23,32,34,43,45,54,56,65,67,76,78,87,89,98,"
                    } else if (sss5.contains("sathaikep")) {
                        str = str.substring(
                            0,
                            str.indexOf("sat")
                        ) + " " + str.substring(i121 + 3, str.length)
                        i121 = 0
                        result += "01,10,12,21,23,32,34,43,45,54,56,65,67,76,78,87,89,98,04,06,51,15,17,60,62,26,28,71,73,37,39,82,84,48,93,95,"
                    } else if (sss5.indexOf("sat2kep") > -1) {
                        str = str.substring(
                            0,
                            str.indexOf("sat")
                        ) + " " + str.substring(i121 + 3, str.length)
                        i121 = 0
                        result += "01,10,12,21,23,32,34,43,45,54,56,65,67,76,78,87,89,98,04,06,51,15,17,60,62,26,28,71,73,37,39,82,84,48,93,95,"
                    }
                } else if (str.indexOf("lech") <= -1 || str.indexOf("lech") >= i121 + 5) {
                    if (str.indexOf("le") <= -1 || str.indexOf("le") >= i121 + 5) {
                        if (str.indexOf("chan") <= -1 || str.indexOf("chan") >= i121 + 5) {
                            if (str.indexOf(" 2 kep") <= -1 || str.indexOf(" 2 kep") >= i121 + 3) {
                                result += "00,11,22,33,44,55,66,77,88,99,"
                                str =
                                    str.substring(0, i121) + " " + str.substring(
                                        i121 + 3
                                    )
                            } else {
                                result += "00,11,22,33,44,55,66,77,88,99,05,50,16,61,27,72,38,83,49,94,"
                                str = str.substring(
                                    0,
                                    i121 - 2
                                ) + " " + str.substring(i121 + 3)
                            }
                        } else if (str.substring(i121, str.indexOf("chan") + 4)
                                .replace(" ", "").replace(",", "")
                                .indexOf("kepchan") > -1
                        ) {
                            str = str.substring(
                                0,
                                i121
                            ) + " " + str.substring(str.indexOf("chan") + 4)
                            result += "00,22,44,66,88,"
                        }
                    } else if (str.substring(i121, str.indexOf("le") + 2)
                            .replace(" ", "").replace(",", "")
                            .indexOf("keple") > -1
                    ) {
                        str = str.substring(
                            0,
                            i121
                        ) + " " + str.substring(str.indexOf("le") + 2)
                        result += "11,33,55,77,99,"
                    }
                } else if (str.substring(i121, str.indexOf("lech") + 4)
                        .replace(" ", "").replace(",", "")
                        .indexOf("keplech") > -1
                ) {
                    str = str.substring(
                        0,
                        i121
                    ) + " " + str.substring(str.indexOf("lech") + 4)
                    result += "05,50,16,61,27,72,38,83,49,94,"
                }
            } else if (str.substring(str.indexOf("sat"), str.indexOf("lech") + 4)
                    .replace(" ", "").replace(",", "")
                    .indexOf("satkeplech") > -1
            ) {
                str =
                    str.substring(0, str.indexOf("sat")) + " " + str.substring(
                        str.indexOf("lech") + 4,
                        str.length
                    )
                i121 = 0
                result += "04,06,51,15,17,60,62,26,28,71,73,37,39,82,84,48,93,95,"
            }
        }
        return str
    }
}