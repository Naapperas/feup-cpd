import java.util.logging.Level;

/**
 * Logger implementation, providing an abstraction layer over the underlying logging framework.
 */
public class Logger {

    /**
     * The logger that we delegate logging to.
     */
    private static final java.util.logging.Logger logger;

    static {
        logger = java.util.logging.Logger.getLogger(Logger.class.getName());
    }

    /**
     * Logs the given message at the INFO level
     *
     * @param message the message to log
     */
    public static void log(String message) {
        Logger.logger.log(Level.INFO, message);
    }

    /**
     * Logs the given message at the ERROR level
     *
     * @param message the message to log
     */
    public static void error(String message) {
        Logger.logger.log(Level.SEVERE, message);
    }
}
