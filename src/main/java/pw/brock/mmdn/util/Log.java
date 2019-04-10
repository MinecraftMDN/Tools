package pw.brock.mmdn.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author BrockWS
 */
public class Log {

    private static final Logger MAIN = LogManager.getLogger("Main");

    public static void fatal(String message) {
        MAIN.fatal(message);
    }

    public static void fatal(String message, Object... params) {
        MAIN.fatal(message, params);
    }

    public static void error(String message) {
        MAIN.error(message);
    }

    public static void error(String message, Object... params) {
        MAIN.error(message, params);
    }

    public static void warn(String message) {
        MAIN.warn(message);
    }

    public static void warn(String message, Object... params) {
        MAIN.warn(message, params);
    }

    public static void info(String message) {
        MAIN.info(message);
    }

    public static void info(String message, Object... params) {
        MAIN.info(message, params);
    }

    public static void debug(String message) {
        MAIN.debug(message);
    }

    public static void debug(String message, Object... params) {
        MAIN.debug(message, params);
    }

    public static void trace(String message) {
        MAIN.trace(message);
    }

    public static void trace(String message, Object... params) {
        MAIN.trace(message, params);
    }
}
