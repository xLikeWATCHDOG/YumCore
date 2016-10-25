package pw.yumc.YumCore.kit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;

public class LogKit implements Runnable {
    private final static Plugin plugin = P.instance;
    private final static File dataFolder = plugin.getDataFolder();

    private PrintStream ps;
    private final List<String> logs = new ArrayList<>(5);

    public LogKit(final File log) {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            final FileOutputStream fos = new FileOutputStream(log, true);
            this.ps = new PrintStream(fos, true, "UTF-8");
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 100);
        } catch (final FileNotFoundException e) {
            Log.debug(e);
            Log.w("日志文件未找到 %s !", e.getMessage());
        } catch (final IOException e) {
            Log.debug(e);
            Log.w("无法创建日志文件 %s !", e.getMessage());
        }

    }

    public LogKit(final String name) {
        this(new File(dataFolder, name));
    }

    /**
     * 关闭日志并保存
     */
    public void close() {
        this.ps.close();
    }

    /**
     * 添加日志
     *
     * @param s
     *            日志
     */
    public void log(final String s) {
        synchronized (logs) {
            logs.add(new Date().toLocaleString() + s);
        }
    }

    /**
     * 添加日志
     *
     * @param s
     *            日志
     */
    public void logConsole(final String s) {
        Log.info(s);
        log(s);
    }

    /**
     * 添加日志
     *
     * @param s
     *            日志
     */
    public void logSender(final CommandSender sender, final String s) {
        sender.sendMessage(s);
        log(ChatColor.stripColor(s));
    }

    /**
     * 添加日志
     *
     * @param s
     *            日志
     */
    public void logSender(final String s) {
        logSender(Bukkit.getConsoleSender(), s);
    }

    @Override
    public void run() {
        synchronized (logs) {
            for (final String s : logs) {
                ps.println(s);
            }
            ps.flush();
            logs.clear();
        }
    }
}