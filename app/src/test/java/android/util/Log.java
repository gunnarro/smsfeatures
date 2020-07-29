package android.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static int d(String tag, String msg) {
        System.out.println(String.format("[%s  DEBUG] [%s] %s", LocalDateTime.now().format(formatter), tag , msg));
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println(String.format("[%s INFO] [%s] %s", LocalDateTime.now().format(formatter), tag , msg));
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println(String.format("[%s WARN] [%s] %s", LocalDateTime.now().format(formatter), tag , msg));
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println(String.format("[%s ERROR] [%s] %s", LocalDateTime.now().format(formatter), tag , msg));
        return 0;
    }

}