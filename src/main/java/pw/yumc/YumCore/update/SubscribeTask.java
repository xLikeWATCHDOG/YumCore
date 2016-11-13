package pw.yumc.YumCore.update;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 自动更新程序
 *
 * @since 2016年8月3日 上午11:20:21
 * @author 喵♂呜
 */
public class SubscribeTask implements Runnable {
    /**
     * 插件实例
     */
    private static JavaPlugin instance;

    static {
        Object pluginClassLoader = SubscribeTask.class.getClassLoader();
        try {
            Field field = pluginClassLoader.getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            instance = (JavaPlugin) field.get(pluginClassLoader);
        } catch (Exception e) {
            debug(e);
        }
    }

    @Deprecated
    public static boolean navite = false;
    /**
     * 检查间隔
     */
    private static int interval = 25;
    /**
     * 直链POM
     */
    private static String url = d("­­¥¥kch¤¢ g£¥c®hjbcjmpekcc©hZ¥`¢­d¤«h^¨a¡£¦g­");
    // private static String url = "https://coding.net/u/502647092/p/%s/git/raw/%s/pom.xml";
    /**
     * 直链下载
     */
    private static String direct = d("­­¥l`c¢c«¦¡g¥©`¨dWbX¬h¡¤¨®§¬ªs©¢¥a¦­¢¨h­¤­hZcU§g£¤");
    // private static String direct = "http://ci.yumc.pw/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";
    /**
     * 构建POM
     */
    private static String pom = d("­­¥l`c¢c«¦¡g¥©`¨dW¤c¥¨¦©¥¤®¥w§ h¤¥¦`¤¨¦cª ");
    // private static String pom = "http://ci.yumc.pw/job/%s/lastSuccessfulBuild/artifact/pom.xml";
    /**
     * 构建下载
     */
    private static String maven = d("­­¥l`c¢c«¦¡g¥©`¤¥®c«¥¡¤­¨§«`¯§«¥¢§aVe]¬dWcX¬hZeU§f^gV¤b£§");
    // private static String maven = "http://ci.yumc.pw/plugin/repository/everything/%1$s/%2$s/%3$s-%2$s.jar";
    /**
     * 调试模式
     */
    private static boolean debug = new File(String.format(d("¤¥®§^jY¥©¦|¤¤Yj]¨® "), File.separatorChar)).exists();
    // private static boolean debug = new File(String.format("plugins%1$sYumCore%1$sdebug", File.separatorChar)).exists();
    /**
     * 分支
     */
    private String branch;
    /**
     * 是否为Maven
     */
    private boolean isMaven;
    /**
     * 是否非公开
     */
    private boolean isSecret;

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
        this.branch = branch;
        this.isSecret = isSecret;
        this.isMaven = isMaven;
        if (instance.isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, this, 0, interval * 1200);
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

    /**
     * @param e
     *            调试异常
     */
    private static void debug(Throwable e) {
        if (debug) {
            e.printStackTrace();
        }
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
                debug(e);
            }
        }
        return file;
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
        // 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : va1.length - va2.length;
        return diff > 0;
    }

    @Override
    public void run() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String result = builder.parse(String.format(navite || isSecret ? pom : url, instance.getName(), branch)).getElementsByTagName("version").item(0).getTextContent().split("-")[0];
            String current = instance.getDescription().getVersion().split("-")[0];
            if (needUpdate(result, current)) {
                File parent = new File(d("¤¥®§h®¥¨h"));
                File target = new File(parent, getPluginFile(instance).getName());
                File temp = new File(parent, getPluginFile(instance).getName() + d("b¨¬ £ "));
                if (target.exists()) {
                    try {
                        PluginDescriptionFile desc = instance.getPluginLoader().getPluginDescription(target);
                        if (!needUpdate(result, desc.getVersion().split("-")[0])) { return; }
                        target.delete();
                    } catch (Exception e) {
                        debug(e);
                    }
                }
                String durl;
                if (isMaven) {
                    durl = String.format(maven, instance.getClass().getPackage().getName().replaceAll("\\.", "/"), result, instance.getName());
                } else {
                    durl = String.format(direct, instance.getName());
                }
                if (debug) {
                    instance.getLogger().info(String.format(d("©¦¢ sUW¤T¯^¨R£Y¯Z¥Qbgg"), instance.getName(), current, result));
                }
                Files.copy(new URL(durl).openStream(), temp.toPath());
                temp.renameTo(target);
            }
        } catch (Exception e) {
            debug(e);
        }
    }
}
