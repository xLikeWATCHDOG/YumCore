package pw.yumc.YumCore.update;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import pw.yumc.YumCore.bukkit.P;

/**
 * 自动更新程序
 *
 * @since 2016年8月3日 上午11:20:21
 * @author 喵♂呜
 */
public class SubscribeTask implements Runnable {
    private final static String url = "https://coding.net/u/502647092/p/%s/git/raw/%s/pom.xml";
    private final static String direct = "http://ci.yumc.pw/job/%1$s/lastSuccessfulBuild/artifact/target/%1$s.jar";
    private final static String maven = "http://ci.yumc.pw/plugin/repository/everything/%1$s/%2$s/%3$s-%2$s.jar";

    private final String branch;
    private final boolean isMaven;

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
        this("master", isMaven);
    }

    /**
     * 自动更新
     *
     * @param branch
     *            更新分支
     */
    public SubscribeTask(final String branch, final boolean isMaven) {
        this.isMaven = isMaven;
        this.branch = branch;
        Bukkit.getScheduler().runTaskTimerAsynchronously(P.instance, this, 0, 24000);
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
            final String result = builder.parse(String.format(url, P.getName(), branch)).getElementsByTagName("version").item(0).getTextContent().split("-")[0];
            if (needUpdate(result, P.getDescription().getVersion().split("-")[0])) {
                final File target = new File("plugins/update/" + getPluginFile(P.instance).getName());
                final File temp = new File("plugins/update/" + getPluginFile(P.instance).getName() + ".downloading");
                if (target.exists()) {
                    try {
                        final PluginDescriptionFile desc = P.instance.getPluginLoader().getPluginDescription(target);
                        if (!needUpdate(result, desc.getVersion().split("-")[0])) {
                            return;
                        }
                        target.delete();
                    } catch (final Exception e) {
                    }
                }
                String durl = null;
                if (isMaven) {
                    durl = String.format(maven, P.instance.getClass().getPackage().getName().replaceAll(".", "/"), result, P.getName());
                } else {
                    durl = String.format(direct, P.getName());
                }
                Files.copy(new URL(durl).openStream(), temp.toPath());
                temp.renameTo(target);
            }
        } catch (final Exception e) {
        }
    }
}
