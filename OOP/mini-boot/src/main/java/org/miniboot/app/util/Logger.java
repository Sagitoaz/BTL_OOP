package org.miniboot.app.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum Level { TRACE, DEBUG, INFO, WARN, ERROR }
    private final String name;
    private static final Level GLOBAL_LEVEL =
            Level.valueOf(System.getProperty("LOG_LEVEL",
                    System.getenv().getOrDefault("LOG_LEVEL","INFO")).toUpperCase());

    public static Logger get(Class<?> cls) { return new Logger(cls.getSimpleName()); }
    private Logger(String name) { this.name = name; }

    private boolean enabled(Level lv) { return lv.ordinal() >= GLOBAL_LEVEL.ordinal(); }

    private void log(Level lv, String msg, Throwable t) {
        if (!enabled(lv)) return;
        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String thread = Thread.currentThread().getName();
        System.out.printf("%s [%s] %-5s %s - %s%n", ts, thread, lv, name, msg);
        if (t != null) t.printStackTrace(System.out);
    }

    public void trace(String m){log(Level.TRACE,m,null);}
    public void debug(String m){log(Level.DEBUG,m,null);}
    public void info (String m){log(Level.INFO ,m,null);}
    public void warn (String m){log(Level.WARN ,m,null);}
    public void error(String m){log(Level.ERROR,m,null);}
    public void error(String m, Throwable t){log(Level.ERROR,m,t);}
}
