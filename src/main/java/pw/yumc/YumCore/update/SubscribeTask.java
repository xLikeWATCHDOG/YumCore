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
    public static JavaPlugin instance;

    static {
        final Object pluginClassLoader = SubscribeTask.class.getClassLoader();
        try {
            final Field field = pluginClassLoader.getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            instance = (JavaPlugin) field.get(pluginClassLoader);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static boolean navite = false;
    /**
     * 调试模式
     */
    public static boolean debug = false;
    /**
     * 检查间隔
     */
    private final static int interval = 25;
    /**
     * 直链POM
     */
    private final static String url = "­­¥¥kch¤¢ g£¥c®hjbcjmpekcc©hZ¥`¢­d¤«h^¨a¡£¦g­";
    // private final static String url = "https://coding.net/u/502647092/p/%s/git/raw/%s/pom.xml";
    /**
     * 直链下载
     */
    private final static String direct = "­­¥l`c¢c«¦¡g¥©`¨dWbX¬h¡¤¨®§¬ªs©¢¥a¦­¢¨h­¤­hZcU§g£¤";
    // private final static String direct = "http://ci.yumc.pw/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";
    /**
     * 构建POM
     */
    private final static String pom = "­­¥l`c¢c«¦¡g¥©`¨dW¤c¥¨¦©¥¤®¥w§ h¤¥¦`¤¨¦cª ";
    // private final static String pom = "http://ci.yumc.pw/job/%s/lastSuccessfulBuild/artifact/pom.xml";
    /**
     * 构建下载
     */
    private final static String maven = "­­¥l`c¢c«¦¡g¥©`¤¥®c«¥¡¤­¨§«`¯§«¥¢§aVe]¬dWcX¬hZeU§f^gV¤b£§";
    // private final static String maven = "http://ci.yumc.pw/plugin/repository/everything/%1$s/%2$s/%3$s-%2$s.jar";
    /**
     * 分支
     */
    private final String branch;
    /**
     * 是否为Maven
     */
    private final boolean isMaven;
    /**
     * 是否非公开
     */
    private final boolean isSecret;

    /**
     * 自动更新
     */
    public SubscribeTask() {
        this(false);
    }

    /**
     * 自动更新
     */
    public SubscribeTask(final boolean isMaven) {
        this("master", false, isMaven);
    }

    /**
     * 自动更新
     */
    public SubscribeTask(final boolean isSecret, final boolean isMaven) {
        this("master", isSecret, isMaven);
    }

    /**
     * 自动更新
     *
     * @param branch
     *            更新分支
     */
    public SubscribeTask(final String branch, final boolean isSecret, final boolean isMaven) {
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
    public String d(final String s) {
        final String key = "499521";
        final StringBuffer str = new StringBuffer();
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
     * 获得插件绝对路径
     *
     * @param plugin
     *            - 插件
     * @return 插件的绝对路径
     */
    public File getPluginFile(final Plugin plugin) {
        File file = null;
        final ClassLoader cl = plugin.getClass().getClassLoader();
        if ((cl instanceof URLClassLoader)) {
            @SuppressWarnings("resource")
            final URLClassLoader ucl = (URLClassLoader) cl;
            final URL url = ucl.getURLs()[0];
            try {
                file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            } catch (final UnsupportedEncodingException e) {
            }
        }
        return file;
    }

    /**
     * 比较版本号
     *
     * @param 新版本
     * @param 旧版本
     * @return 是否需要更新
     */
    public boolean needUpdate(final String v1, final String v2) {
        final String[] va1 = v1.split("\\.");// 注意此处为正则匹配，不能用"."；
        final String[] va2 = v2.split("\\.");
        int idx = 0;
        final int minLength = Math.min(va1.length, va2.length);// 取最小长度值
        int diff = 0;
        while (idx < minLength
                && (diff = va1[idx].length() - va2[idx].length()) == 0// 先比较长度
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
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final String result = builder.parse(String.format(navite || isSecret ? d(pom) : d(url), instance.getName(), branch)).getElementsByTagName("version").item(0).getTextContent().split("-")[0];
            if (needUpdate(result, instance.getDescription().getVersion().split("-")[0])) {
                final File parent = new File(d("¤¥®§h®¥¨h"));
                final File target = new File(parent, getPluginFile(instance).getName());
                final File temp = new File(parent, getPluginFile(instance).getName() + d("b¨¬ £ "));
                if (target.exists()) {
                    try {
                        final PluginDescriptionFile desc = instance.getPluginLoader().getPluginDescription(target);
                        if (!needUpdate(result, desc.getVersion().split("-")[0])) {
                            return;
                        }
                        target.delete();
                    } catch (final Exception e) {
                    }
                }
                String durl = null;
                if (isMaven) {
                    durl = String.format(d(maven), instance.getClass().getPackage().getName().replaceAll("\\.", "/"), result, instance.getName());
                } else {
                    durl = String.format(d(direct), instance.getName());
                }
                Files.copy(new URL(durl).openStream(), temp.toPath());
                temp.renameTo(target);
            }
        } catch (final Exception e) {
            if (debug) {
                e.printStackTrace();
            }
        }
    }
}
