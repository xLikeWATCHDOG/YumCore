package pw.yumc.YumCore.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import pw.yumc.YumCore.bukkit.Log;

/**
 * 远程配置文件类
 *
 * @since 2016年2月22日 上午8:33:51
 * @author 喵♂呜
 */
public class RemoteConfig extends FileConfig {

    protected static String REMOTEFILECENTER = "http://data.yumc.pw/config/";

    private static final String PLUGINHELPER = "PluginHelper";
    private static final String fromYumc = "配置 %s 来自 YUMC 数据中心...";
    private static final String createError = "尝试从 YUMC 数据中心下载 %s 失败 部分功能可能无法使用...";
    private static final String updateError = "尝试从 YUMC 数据中心更新配置文件 %s 失败 部分数据可能已过时...";

    public RemoteConfig(final String filename) {
        this(filename, REMOTEFILECENTER + filename);
    }

    public RemoteConfig(final String filename, final String url) {
        this(filename, url, false);
    }

    public RemoteConfig(final String filename, final String url, final boolean force) {
        file = new File(plugin.getDataFolder().getParentFile(), PLUGINHELPER + File.separator + filename);
        if (!file.exists()) {
            try {
                // 尝试从YUMC下载配置文件
                Files.copy(new URL(url).openStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Log.info(String.format(fromYumc, filename));
            } catch (final IOException e) {
                Log.warning(String.format(createError, filename));
            }
        } else {
            final FileConfig oldcfg = new FileConfig(file);
            final FileConfig newcfg = getFileConfig(url);
            final String newver = newcfg.getString(VERSION);
            final String oldver = oldcfg.getString(VERSION);
            if (newver != null && !newver.equals(oldver)) {
                try {
                    file.renameTo(new File(plugin.getDataFolder().getParentFile(), PLUGINHELPER + File.separator + getBakName(filename)));
                    updateConfig(newcfg, oldcfg, force).save(file);
                } catch (final IOException e) {
                    Log.warning(String.format(updateError, filename));
                }
            }
        }
        init(file);
    }

    /**
     * 获得配置文件
     *
     * @param url
     *            配置文件地址
     * @return {@link FileConfig}
     */
    public static FileConfig getFileConfig(final String url) {
        final FileConfig config = new FileConfig();
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream(), UTF_8));
            config.load(br);
            br.close();
        } catch (final Exception e) {
        }
        return config;
    }

    /**
     * 获得Yaml文件标签信息
     *
     * @param url
     *            XML文件地址
     * @param tag
     *            信息标签
     * @param def
     *            默认值
     * @return 插件信息
     */
    public static String getYamlTag(final String url, final String tag, final String def) {
        String result = def;
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream(), UTF_8));
            final FileConfiguration config = YamlConfiguration.loadConfiguration(br);
            br.close();
            result = config.getString(tag);
        } catch (final Exception e) {
        }
        return result;
    }
}
