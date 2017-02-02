package pw.yumc.YumCore.update;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.tellraw.Tellraw;

/**
 * 自动更新程序
 *
 * @since 2016年8月3日 上午11:20:21
 * @author 喵♂呜
 */
public class SubscribeTask implements Runnable, Listener {
    /**
     * 插件实例
     */
    private static JavaPlugin instance;
    private static String version;

    static {
        try {
            Object pluginClassLoader = SubscribeTask.class.getClassLoader();
            Field field = pluginClassLoader.getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            instance = (JavaPlugin) field.get(pluginClassLoader);
            version = instance.getDescription().getVersion();
        } catch (Exception e) {
            Log.d(e);
        }
    }

    /**
     * 检查间隔
     */
    private static int interval = 25;

    /**
     * 直链下载
     */
    private static String direct = d("­­¥l`c¢c«¦¡g¥©`¨dWbX¬h¡¤¨®§¬ªs©¢¥a¦­¢¨h­¤­hZcU§g£¤");
    // private static String direct = "http://ci.yumc.pw/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";

    /**
     * 构建下载
     */
    private static String maven = d("­­¥l`c¢c«¦¡g¥©`¤¥®c«¥¡¤­¨§«`¯§«¥¢§aVe]¬dWcX¬hZeU§f^gV¤b£§");
    // private static String maven = "http://ci.yumc.pw/plugin/repository/everything/%1$s/%2$s/%3$s-%2$s.jar";
    /**
     * 是否为Maven
     */
    private boolean isMaven;
    /**
     * 更新文件
     */
    private UpdateFile updateFile;
    /**
     * 版本信息
     */
    private VersionInfo versionInfo;

    /**
     * 自动更新
     */
    public SubscribeTask() {
        this(false);
    }

    /**
     * 自动更新
     *
     * @param isMaven
     *            是否为Maven
     */
    public SubscribeTask(boolean isMaven) {
        this(false, isMaven);
    }

    /**
     * 自动更新
     *
     * @param isSecret
     *            是否为私有
     * @param isMaven
     *            是否为Maven
     */
    public SubscribeTask(boolean isSecret, boolean isMaven) {
        this("master", isSecret, isMaven);
    }

    /**
     * 自动更新
     *
     * @param branch
     *            更新分支
     * @param isSecret
     *            是否为私有
     * @param isMaven
     *            是否为Maven
     */
    public SubscribeTask(String branch, boolean isSecret, boolean isMaven) {
        updateFile = new UpdateFile(instance);
        versionInfo = new VersionInfo(branch, isSecret);
        this.isMaven = isMaven;
        if (instance.isEnabled()) {
            Bukkit.getPluginManager().registerEvents(this, instance);
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, this, 0, interval * 1200);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        if (player.isOp() && updateFile.isUpdated()) {
            Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
                @Override
                public void run() {
                    versionInfo.notify(player);
                }
            }, 10);
        }
    }

    /**
     * 解密地址
     *
     * @param s
     *            密串
     * @return 解密后的地址
     */
    public static String d(String s) {
        String key = "499521";
        StringBuilder str = new StringBuilder();
        int ch;
        for (int i = 0, j = 0; i < s.length(); i++, j++) {
            if (j > key.length() - 1) {
                j = j % key.length();
            }
            ch = (s.codePointAt(i) + 65535 - key.codePointAt(j));
            if (ch > 65535) {
                ch = ch % 65535;// ch - 33 = (ch - 33) % 95 ;
            }
            str.append((char) ch);
        }
        return str.toString();
    }

    @Override
    public void run() {
        update();
    }

    public void update() {
        try {
            versionInfo.update();
            String result = versionInfo.getNewVersion();
            if (result != null) {
                if (updateFile.target.exists()) {
                    try {
                        PluginDescriptionFile desc = instance.getPluginLoader().getPluginDescription(updateFile.target);
                        if (!versionInfo.needUpdate(result, desc.getVersion().split("-")[0])) { return; }
                        updateFile.target.delete();
                    } catch (Exception e) {
                        Log.d(e);
                    }
                }
                String download;
                if (isMaven) {
                    download = String.format(maven, instance.getClass().getPackage().getName().replaceAll("\\.", "/"), result, instance.getName());
                } else {
                    download = String.format(direct, instance.getName());
                }
                updateFile.update(download);
                Log.d(d("©¦¢ sUW¤T¯^¨R£Y¯Z¥Qbgg"), instance.getName(), version.split("-")[0], result);
                versionInfo.notify(Bukkit.getConsoleSender());
            }
        } catch (Exception e) {
            Log.d(e);
        }
    }

    static class UpdateFile {
        public File parent;
        public File target;
        public File temp;

        public UpdateFile(Plugin plugin) {
            String name = getPluginFile(plugin).getName();
            parent = new File(d("¤¥®§h®¥¨h"));
            if (!parent.exists()) {
                parent.mkdirs();
            }
            target = new File(parent, name);
            temp = new File(parent, name + d("b¨¬ £ "));
        }

        public boolean isUpdated() {
            return target.exists();
        }

        /**
         * 获得插件绝对路径
         *
         * @param plugin
         *            - 插件
         * @return 插件的绝对路径
         */
        public File getPluginFile(Plugin plugin) {
            File file = null;
            ClassLoader cl = plugin.getClass().getClassLoader();
            if ((cl instanceof URLClassLoader)) {
                URLClassLoader ucl = (URLClassLoader) cl;
                URL url = ucl.getURLs()[0];
                try {
                    file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.d(e);
                }
            }
            return file;
        }

        public void update(String url) throws IOException {
            Files.copy(new URL(url).openStream(), temp.toPath());
            temp.renameTo(target);
        }
    }

    static class VersionInfo {
        /**
         * 直链POM
         */
        private String url = d("­­¥¥kch¤¢ g£¥c®hjbcjmpekcc©hZ¥`¢­d¤«h^¨a¡£¦g­");
        // private static String url = "https://coding.net/u/502647092/p/%s/git/raw/%s/pom.xml";
        /**
         * 构建POM
         */
        private String pom = d("­­¥l`c¢c«¦¡g¥©`¨dW¤c¥¨¦©¥¤®¥w§ h¤¥¦`¤¨¦cª ");
        // private static String pom = "http://ci.yumc.pw/job/%s/lastSuccessfulBuild/artifact/pom.xml";

        /**
         * 插件信息地址
         */
        private String info;

        /**
         * POM文件文档
         */
        private Document document;

        public VersionInfo(String branch, boolean isSecret) {
            info = String.format(isSecret ? pom : url, instance.getName(), branch);
        }

        /**
         * 获得插件信息
         * 
         * @param tag
         *            数据标签
         * @param def
         *            默认值
         * @return 信息
         */
        public String getPluginInfo(String tag, String def) {
            String result = def;
            try {
                result = document.getElementsByTagName(tag).item(0).getTextContent();
            } catch (Exception e) {
                Log.d(e);
            }
            return result;
        }

        /**
         * 获得插件更新记录
         *
         * @return 插件更新记录
         */
        public String[] getUpdateChanges() {
            final String des = getPluginInfo("update.changes", "无版本更新信息...");
            return ChatColor.translateAlternateColorCodes('&', des).replaceAll("\n", "").replaceAll("\u0009", "").split(";");
        }

        /**
         * 获得插件更新信息
         *
         * @return 插件更新信息
         */
        public String getUpdateDescription() {
            final String des = getPluginInfo("update.description", "无版本描述信息...");
            return ChatColor.translateAlternateColorCodes('&', des);
        }

        /**
         * 获得最新的版本
         *
         * @return 最后版本
         */
        private String getLastestVersion() {
            return getPluginInfo("version", "0.0.0").split("-")[0];
        }

        /**
         * 通知更新信息
         * 
         * @param sender
         *            命令接受者
         */
        public void notify(CommandSender sender) {
            Log.sender(sender, "§a插件更新: §b" + instance.getName() + " §a已更新到最新版本 §bv" + getLastestVersion());
            Log.sender(sender, "§e版本简介: §a" + getUpdateDescription());
            final String[] changes = getUpdateChanges();
            if (changes.length != 0) {
                if (sender instanceof Player) {
                    Tellraw tr = Tellraw.create(Log.getPrefix() + "§b更新记录: ");
                    tr.then("§d§n鼠标悬浮查看");
                    tr.tip(changes);
                    tr.send(sender);
                } else {
                    Log.sender(sender, "§b更新记录:");
                    Log.sender(sender, changes);
                }
            }
            Log.sender(sender, "§c最新的改动将在重启后生效!");
        }

        /**
         * 比较版本号
         *
         * @param v1
         *            新版本
         * @param v2
         *            旧版本
         * @return 是否需要更新
         */
        public boolean needUpdate(String v1, String v2) {
            String[] va1 = v1.split("\\.");// 注意此处为正则匹配，不能用"."；
            String[] va2 = v2.split("\\.");
            int idx = 0;
            int minLength = Math.min(va1.length, va2.length);// 取最小长度值
            int diff = 0;
            while (idx < minLength && (diff = va1[idx].length() - va2[idx].length()) == 0// 先比较长度
                    && (diff = va1[idx].compareTo(va2[idx])) == 0) {// 再比较字符
                ++idx;
            }
            // 如果已经分出大小 则直接返回 如果未分出大小 则再比较位数 有子版本的为大
            diff = (diff != 0) ? diff : va1.length - va2.length;
            return diff > 0;
        }

        public String getNewVersion() {
            try {
                String result = getLastestVersion();
                if (version.contains("DEV") && !Log.isGlobalDebug()) {
                    Log.console("§4注意: §c当前版本为开发版本 且未开启全局调试 已自动下载最新稳定版!");
                    return result;
                }
                String current = version.split("-")[0];
                if (needUpdate(result, current)) { return result; }
            } catch (Exception e) {
                Log.d(e);
            }
            return null;
        }

        public void update() throws ParserConfigurationException, IOException, SAXException {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(info);
        }
    }
}
