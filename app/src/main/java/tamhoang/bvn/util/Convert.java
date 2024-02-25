package tamhoang.bvn.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Convert {

    static public String versionCodeToDate(int versionCode) {
        int year = versionCode / 10000;
        int month = (versionCode % 10000) / 100;
        int date = (versionCode % 10000) % 100;
        return date + "/" + month + "/" + year;
    }

    static public String convertToLatin(String str) {
        String result = str;
        final String transl = "aaaaaaaaaaaaaaaaadeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";
        final String origin = "àáảãạăắằẵặẳâầấậẫẩđèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ";
        for (int i = 0; i < transl.length(); i++) {
            result = result.replace(origin.charAt(i), transl.charAt(i));
        }
        return result;
    }

    static public String dateIntToString1(long dateInt) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(dateInt, 0, ZoneOffset.ofHours(7));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    static public String dateIntToString(long dateInt) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(dateInt, 0, ZoneOffset.ofHours(7));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }
}
