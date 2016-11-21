package pw.yumc.YumCore.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 插件日志输出类
 *
 * @author 喵♂呜
 * @since 2016年7月23日 上午9:11:01
 */
public class Log {
    private static boolean debug = new File(String.format("plugins%1$sYumCore%1$sdebug", File.separatorChar)).exists();
    private static Logger logger = P.instance.getLogger();
    private static CommandSender console = Bukkit.getConsoleSender();
    private static String prefix = String.format("§6[§b%s§6]§r ", P.instance.getName());

    private Log() {
    }

    /**
     * Add a log Handler to receive logging messages.
     * <p>
     * By default, Loggers also send their output to their parent logger.
     * Typically the root Logger is configured with a set of Handlers
     * that essentially act as default handlers for all loggers.
     *
     * @param handler a logging Handler
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    public static void addHandler(Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }

    /**
     * Sends console a message
     *
     * @param message Message to be displayed
     */
    public static void console(String message) {
        console.sendMessage(prefix + message);
    }

    /**
     * Sends console a message
     *
     * @param message 消息
     * @param object  格式化参数
     */
    public static void console(String message, Object... object) {
        console.sendMessage(prefix + String.format(message, object));
    }

    /**
     * Sends console a message
     *
     * @param msg Message to be displayed
     */
    public static void console(String[] msg) {
        for (String str : msg) {
            console(str);
        }
    }

    /**
     * 调试消息
     *
     * @param msg    消息
     * @param object 参数
     */
    public static void d(String msg, Object... object) {
        debug(String.format(msg, object));
    }

    /**
     * 格式化调试消息
     *
     * @param msg 消息
     */
    public static void debug(String msg) {
        if (debug) {
            logger.info("[DEBUG] " + msg);
        }
    }

    /**
     * 调试消息
     *
     * @param msg    消息
     * @param object 参数
     */
    public static void debug(String msg, Object... object) {
        if (debug) {
            logger.log(Level.SEVERE, "[DEBUG] " + msg, object);
        }
    }

    /**
     * 调试消息
     *
     * @param msg 消息
     * @param e   异常
     */
    public static void debug(String msg, Throwable e) {
        if (debug) {
            logger.info("[DEBUG] " + msg);
            e.printStackTrace();
        }
    }

    /**
     * 调试消息
     *
     * @param e 异常
     */
    public static void debug(Throwable e) {
        if (debug) {
            e.printStackTrace();
        }
    }

    /**
     * @return 获得插件前缀
     */
    public static String getPrefix() {
        return prefix;
    }

    public static void i(String msg, Object... objs) {
        logger.info(String.format(msg, objs));
    }

    /**
     * Log an INFO message.
     * <p>
     * If the logger is currently enabled for the INFO message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * Log a message, with no arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param level One of the message level identifiers, e.g., SEVERE
     * @param msg   The string message (or a key in the message catalog)
     */
    public static void log(Level level, String msg) {
        logger.log(level, msg);
    }

    /**
     * Log a message, with one object parameter.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     *
     * @param level  One of the message level identifiers, e.g., SEVERE
     * @param msg    The string message (or a key in the message catalog)
     * @param param1 parameter to the message
     */
    public static void log(Level level, String msg, Object param1) {
        logger.log(level, msg, param1);
    }

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     *
     * @param level  One of the message level identifiers, e.g., SEVERE
     * @param msg    The string message (or a key in the message catalog)
     * @param params array of parameters to the message
     */
    public static void log(Level level, String msg, Object[] params) {
        logger.log(level, msg, params);
    }

    /**
     * Log a message, with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property. Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     *
     * @param level  One of the message level identifiers, e.g., SEVERE
     * @param msg    The string message (or a key in the message catalog)
     * @param thrown Throwable associated with log message.
     */
    public static void log(Level level, String msg, Throwable thrown) {
        logger.log(level, msg, thrown);
    }

    /**
     * @param prefix 插件前缀
     */
    public static void setPrefix(String prefix) {
        Log.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
    }

    /**
     * Log a SEVERE message.
     * <p>
     * If the logger is currently enabled for the SEVERE message level then the given message is forwarded to all the registered output Handler objects.
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public static void severe(String msg) {
        logger.severe(msg);
    }

    /**
     * Sends this sender a message
     *
     * @param sender 命令发送者
     * @param msg    消息
     */
    public static void sender(CommandSender sender, String msg) {
        sender.sendMessage(prefix + msg);
    }

    /**
     * Sends this sender a message
     *
     * @param sender 命令发送者
     * @param msg    消息
     * @param objs   参数
     */
    public static void sender(CommandSender sender, String msg, Object... objs) {
        sender.sendMessage(prefix + String.format(msg, objs));
    }

    /**
     * Sends this sender a message
     *
     * @param sender 命令发送者
     * @param msg    消息
     */
    public static void sender(CommandSender sender, String[] msg) {
        for (String str : msg) {
            sender(sender, str);
        }
    }

    /**
     * 格式化警告消息
     *
     * @param string  消息
     * @param objects 参数
     */
    public static void w(String string, Object... objects) {
        logger.warning(String.format(string, objects));
    }

    /**
     * Log a WARNING message.
     * <p>
     * If the logger is currently enabled for the WARNING message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     *
     * @param msg The string message (or a key in the message catalog)
     */
    public static void warning(String msg) {
        logger.warning(msg);
    }

}
