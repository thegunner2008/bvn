package tamhoang.bvn.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Util {
    static final int BUFFER_SIZE = 2048;
    public static String DIRECTORY_PATH = (Environment.getExternalStorageDirectory() + File.separator + "ldpro_logs.txt");
    public static boolean ON = false;
    static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static void writeLog(Exception exc) {
        if (ON) {
            checkFileSize();
            String format = formatter.format(new Date());
            try {
                String stackTraceString = Log.getStackTraceString(exc);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(DIRECTORY_PATH), true), StandardCharsets.UTF_8));
                bufferedWriter.append(format).append(":\n");
                bufferedWriter.append(stackTraceString).append("\n");
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeLogInfo(String str) {
        if (ON) {
            checkFileSize();
            String format = formatter.format(new Date());
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(DIRECTORY_PATH), true), StandardCharsets.UTF_8));
                bufferedWriter.append(format).append(":").append(str).append("\n");
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void checkFileSize() {
        try {
            File file = new File(DIRECTORY_PATH);
            if ((file.length() / 1024) / 1024 > 5) {
                String format = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                file.renameTo(new File(Environment.getExternalStorageDirectory() + File.separator + "ldpro_logs_" + format + ".txt"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String str, String str2) throws IOException {
        FileOutputStream fileOutputStream;
        try {
            File file = new File(str2);
            if (!file.isDirectory()) {
                file.mkdirs();
            }
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(str));
            while (true) {
                try {
                    ZipEntry nextEntry = zipInputStream.getNextEntry();
                    if (nextEntry != null) {
                        String str3 = str2 + nextEntry.getName();
                        if (nextEntry.isDirectory()) {
                            File file2 = new File(str3);
                            if (!file2.isDirectory()) {
                                file2.mkdirs();
                            }
                        } else {
                            fileOutputStream = new FileOutputStream(str3, false);
                            while (true) {
                                int read = zipInputStream.read();
                                if (read == -1) {
                                    break;
                                }
                                fileOutputStream.write(read);
                            }
                            zipInputStream.closeEntry();
                            fileOutputStream.close();
                        }
                    } else {
                        zipInputStream.close();
                        return;
                    }
                } catch (Throwable th) {
                    zipInputStream.close();
                    throw th;
                }
            }
        } catch (Exception unused) {
        }
    }
}
