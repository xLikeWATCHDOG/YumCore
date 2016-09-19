package pw.yumc.YumCore.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import pw.yumc.YumCore.bukkit.Log;

/**
 * 远程配置文件类
 *
 * @since 2016年2月22日 上午8:33:51
 * @author 喵♂呜
 */
public class RemoteConfig extends FileConfig {
    public RemoteConfig(final String url) throws MalformedURLException, IOException {
        this(new URL(url));
    }

    public RemoteConfig(final URL url) throws IOException {
        super(url.openStream());
    }

    /**
     * 获得配置文件(错误返回null)
     *
     * @param url
     *            配置文件地址
     * @return {@link FileConfig}
     */
    public static FileConfig getConfig(final String url) {
        try {
            return new RemoteConfig(url);
        } catch (final IOException e) {
            Log.debug("获取远程配置文件失败!", e);
            return null;
        }
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
            result = getConfig(url).getString(tag);
        } catch (final NullPointerException e) {
            // Ignore
        }
        return result;
    }

}
