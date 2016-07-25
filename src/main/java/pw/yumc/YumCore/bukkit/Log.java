package pw.yumc.YumCore.bukkit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * 插件日志输出类
 *
 * @since 2016年7月23日 上午9:11:01
 * @author 喵♂呜
 */
public class Log {
    private static Logger logger = P.instance.getLogger();
    private static String prefix = String.format("§6[§b%s§6]§r ", P.instance.getName());
    private static CommandSender console = Bukkit.getConsoleSender();

    public static void addHandler(final Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }

    public static void console(final String... msg) {
        for (final String str : msg) {
            console.sendMessage(prefix + str);
        }
    }

    public static void info(final String msg) {
        logger.info(msg);
    }

    public static void log(final Level level, final String msg) {
        logger.log(level, msg);
    }

    public static void log(final Level level, final String msg, final Object param1) {
        logger.log(level, msg, param1);
    }

    public static void log(final Level level, final String msg, final Object[] params) {
        logger.log(level, msg, params);
    }

    public static void log(final Level level, final String msg, final Throwable thrown) {
        logger.log(level, msg, thrown);
    }

    public static void severe(final String msg) {
        logger.severe(msg);
    }

    public static void warning(final String msg) {
        logger.warning(msg);
    }

}
