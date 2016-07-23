package pw.yumc.YumCore.bukkit;

import java.lang.reflect.Field;

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
}
