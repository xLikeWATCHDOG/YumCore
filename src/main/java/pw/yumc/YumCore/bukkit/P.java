package pw.yumc.YumCore.bukkit;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
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

    public PluginCommand getCommand(final String name) {
        return instance.getCommand(name);
    }

    public FileConfiguration getConfig() {
        return instance.getConfig();
    }

    public final Logger getLogger() {
        return instance.getLogger();
    }

    public final String getName() {
        return instance.getName();
    }
}
