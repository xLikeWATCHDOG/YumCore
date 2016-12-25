package pw.yumc.YumCore.bukkit;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    /**
     * 插件配置方法
     */
    public static Method getInjectConfigMethod;

    static {
        Object pluginClassLoader = P.class.getClassLoader();
        try {
            Field field = pluginClassLoader.getClass().getDeclaredField("plugin");
            field.setAccessible(true);
            instance = (JavaPlugin) field.get(pluginClassLoader);
        } catch (Exception e) {
            Log.d(e);
        }
        try {
            getInjectConfigMethod = instance.getClass().getMethod("get" + instance.getName() + "Config");
        } catch (NoSuchMethodException e) {
            Log.d(e);
        }
    }

    /**
     * @param name
     *            命令名称
     *
     * @return 插件命令
     */
    public static PluginCommand getCommand(String name) {
        return instance.getCommand(name);
    }

    /**
     * @param <FC>
     *            配置源类型
     * @return 获得插件配置文件
     */
    public static <FC> FC getConfig() {
        return (FC) instance.getConfig();
    }

    /**
     * @param <FC>
     *            注入配置源类型
     * @return 获得插件注入配置
     */
    public static <FC> FC getInjectConfig() {
        try {
            return (FC) getInjectConfigMethod.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
        return getConfig();
    }

    /**
     * @return 获得插件文件夹
     */
    public static File getDataFolder() {
        return instance.getDataFolder();
    }

    /**
     * @return 获得插件描述文件
     */
    public static PluginDescriptionFile getDescription() {
        return instance.getDescription();
    }

    /**
     * @return 获得插件日志器
     */
    public static Logger getLogger() {
        return instance.getLogger();
    }

    /**
     * @return 插件名称
     */
    public static String getName() {
        return instance.getName();
    }

    /**
     * @param <PI>
     *            插件源类型
     * @return 获得插件
     */
    public static <PI> PI getPlugin() {
        return (PI) instance;
    }

    /**
     * @return 插件是否已启用
     */
    public static boolean isEnabled() {
        return instance.isEnabled();
    }

}
