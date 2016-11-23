package pw.yumc.YumCore.config.ext;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.config.FileConfig;

import java.io.IOException;
import java.net.URL;

/**
 * 远程配置文件类
 *
 * @since 2016年2月22日 上午8:33:51
 * @author 喵♂呜
 */
public class RemoteConfig extends FileConfig {
    public RemoteConfig(String url) throws IOException {
        this(new URL(url));
    }

    public RemoteConfig(URL url) throws IOException {
        super(url.openStream());
    }

    /**
     * 获得配置文件(错误返回null)
     *
     * @param url
     *            配置文件地址
     * @return {@link FileConfig}
     */
    public static FileConfig getConfig(String url) {
        try {
            return new RemoteConfig(url);
        } catch (IOException e) {
            Log.d("获取远程配置文件失败!", e);
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
    public static String getYamlTag(String url, String tag, String def) {
        String result = def;
        FileConfig config = getConfig(url);
        if (config != null) {
            result = config.getString(tag);
        }
        return result;
    }

}
