package tamhoang.bvn.data.map

object MapSo {

    val clearChia = hashMapOf(
        "tong ko chia" to "ko chia 3",
        "tong chia 3 du 1" to "chia 3 du 1",
        "tong chia 3 du 2" to "chia 3 du 2"
    )

    val vietTat = hashMapOf(
        "tong > 10" to
                "so 29,38,39,47,48,49,56,57,58,59,65,66,67,68,69,74,75,76,77,78,79,83,84,85,86,87,88,89,92,93,94,95,96,97,98,99,",

        "tong < 10 " to
                "so 01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,20,21,22,23,24,25,26,27,30,31,32,33,34,35,36,40,41,42,43,44,45,50,51,52,53,54,60,61,62,63,70,71,72,80,81,90,",

        "dau > dit " to
                "so 10,20,21,30,31,32,40,41,42,43,50,51,52,53,54,60,61,62,63,64,65,70,71,72,73,74,75,76,80,81,82,83,84,85,86,87,90,91,92,93,94,95,96,97,98,",

        "dit < dau " to
                "so 10,20,21,30,31,32,40,41,42,43,50,51,52,53,54,60,61,62,63,64,65,70,71,72,73,74,75,76,80,81,82,83,84,85,86,87,90,91,92,93,94,95,96,97,98,",

        "dau < dit " to
                "so 01,02,03,04,05,06,07,08,09,12,13,14,15,16,17,18,19,23,24,25,26,27,28,29,34,35,36,37,38,39,45,46,47,48,49,56,57,58,59,67,68,69,78,79,89,",

        "dit > dau " to
                "so 01,02,03,04,05,06,07,08,09,12,13,14,15,16,17,18,19,23,24,25,26,27,28,29,34,35,36,37,38,39,45,46,47,48,49,56,57,58,59,67,68,69,78,79,89,",

        "ko chia 3 " to
                "so 00,01,04,07,10,13,16,19,22,25,28,31,34,37,40,43,46,49,52,55,58,61,64,67,70,73,76,79,82,85,88,91,94,97,02,05,08,11,14,17,20,23,26,29,32,35,38,41,44,47,50,53,56,59,62,65,68,71,74,77,80,83,86,89,92,95,98,",

        "tong chia 3 " to
                "so 03,06,09,12,15,18,21,24,27,30,33,36,39,42,45,48,51,54,57,60,63,66,69,72,75,78,81,84,87,90,93,96,99, ",

        "chia 3 du 1 " to
                "so 01,04,07,10,13,16,19,22,25,28,31,34,37,40,43,46,49,52,55,58,61,64,67,70,73,76,79,82,85,88,91,94,97, ",

        "chia 3 du 2 " to
                "so 02,05,08,11,14,17,20,23,26,29,32,35,38,41,44,47,50,53,56,59,62,65,68,71,74,77,80,83,86,89,92,95,98, "
    )

    val kyTu = hashMapOf(
        ":" to " ",
        ";" to " ",
        " ," to ", "
    )

    val toNho = hashMapOf(
        "toto" to "55,56,57,58,59,65,66,67,68,69,75,76,77,78,79,85,86,87,88,89,95,96,97,98,99,",
        "tonho" to "50,51,52,53,54,60,61,62,63,64,70,71,72,73,74,80,81,82,83,84,90,91,92,93,94,",
        "nhoto" to "05,06,07,08,09,15,16,17,18,19,25,26,27,28,29,35,36,37,38,39,45,46,47,48,49,",
        "nhonho" to "00,01,02,03,04,10,11,12,13,14,20,21,22,23,24,30,31,32,33,34,40,41,42,43,44,"
    )

    val chanLe = hashMapOf(
        "chanchan" to "00,02,04,06,08,20,22,24,26,28,40,42,44,46,48,60,62,64,66,68,80,82,84,86,88,",
        "chanle" to "01,03,05,07,09,21,23,25,27,29,41,43,45,47,49,61,63,65,67,69,81,83,85,87,89,",
        "lele" to "11,13,15,17,19,31,33,35,37,39,51,53,55,57,59,71,73,75,77,79,91,93,95,97,99,",
        "lechan" to "10,12,14,16,18,30,32,34,36,38,50,52,54,56,58,70,72,74,76,78,90,92,94,96,98,"
    )
}