package pw.yumc.YumCore.bukkit;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 插件Instance获取类
 *
 * @since 2016年7月23日 上午9:09:57
 * @author 喵♂呜
 */
public class P {
    /**
     * 插件实例
     */
    public static JavaPlugin instance;

    static {
        final Object pluginClassLoader = P.class.getClassLoader();
        try {
            final Field field = pluginClassLoader.getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            instance = (JavaPlugin) field.get(pluginClassLoader);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param name
     *            命令名称
     *
     * @return 插件命令
     */
    public static PluginCommand getCommand(final String name) {
        return instance.getCommand(name);
    }

    /**
     * @return 获得插件配置文件
     */
    @SuppressWarnings("unchecked")
    public static <FC> FC getConfig() {
        return (FC) instance.getConfig();
    }

    /**
     * @return 获得插件文件夹
     */
    public static final File getDataFolder() {
        return instance.getDataFolder();
    }

    /**
     * @return 获得插件描述文件
     */
    public static final PluginDescriptionFile getDescription() {
        return instance.getDescription();
    }

    /**
     * @return 获得插件日志器
     */
    public static final Logger getLogger() {
        return instance.getLogger();
    }

    /**
     * @return 插件名称
     */
    public static final String getName() {
        return instance.getName();
    }

    /**
     * @return 获得插件
     */
    @SuppressWarnings("unchecked")
    public static <PI> PI getPlugin() {
        return (PI) instance;
    }

    /**
     * @return 插件是否已启用
     */
    public static final boolean isEnabled() {
        return instance.isEnabled();
    }

}
