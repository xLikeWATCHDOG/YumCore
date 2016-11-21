package pw.yumc.YumCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 命令工具类
 * 
 * @author 蒋天蓓
 * @since 2016/11/21 0021.
 */
public class CommandKit {
    private static Constructor<PluginCommand> PluginCommandConstructor;
    private static Map<String, Command> knownCommands;
    private static Map<String, Plugin> lookupNames;
    static {
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();

            Field lookupNamesField = pluginManager.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            lookupNames = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            PluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        } catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.d("初始化命令管理器失败!");
            Log.debug(e);
        }
    }

    public static PluginCommand create(String name) {
        return create(name, P.instance);
    }

    public static PluginCommand create(String name, JavaPlugin plugin) {
        try {
            knownCommands.put(name, PluginCommandConstructor.newInstance(name, plugin));
            lookupNames.put(name, plugin);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
        }
        return plugin.getCommand(name);
    }
}
