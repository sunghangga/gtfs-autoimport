package com.maestronic.autoimportgtfs.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    public static void info(String message) {
        System.out.println(LocalDateTime.now().format(formatter) + " [INFO] : " + message);
    }

    public static void warn(String message) {
        System.out.println(LocalDateTime.now().format(formatter) + " [WARN] : " + message);
    }

    public static void error(String message) {
        System.out.println(LocalDateTime.now().format(formatter) + " [ERROR] : " + message);
    }
}
