package pw.yumc.YumCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.info.CommandInfo;
import pw.yumc.YumCore.commands.interfaces.Executor;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 主类命令管理
 * 
 * @author 喵♂呜
 * @since 2016/11/18 0018
 */
public class CommandMain implements CommandExecutor {
    private static String argumentTypeError = "注解命令方法 %s 位于 %s 的参数错误 第一个参数应实现 CommandSender 接口!";
    /**
     * 命令列表
     */
    private Set<CommandInfo> cmds = new HashSet<>();
    /**
     * 命令缓存列表
     */
    private Map<String, CommandInfo> cmdCache = new HashMap<>();
    /**
     * 命令帮助处理
     */
    private CommandHelp help;

    /**
     * 主类命令管理类
     * 
     * @param clazzs
     *            命令类
     */
    public CommandMain(Executor... clazzs) {
        register(clazzs);
    }

    /**
     * 注册命令
     * 
     * @param clazzs
     *            命令类
     * @return {@link CommandMain}
     */
    public CommandMain register(Executor... clazzs) {
        for (Executor clazz : clazzs) {
            Method[] methods = clazz.getClass().getDeclaredMethods();
            for (Method method : methods) {
                registerCommand(method, clazz);
            }
        }
        help = new CommandHelp(cmds);
        return this;
    }

    private boolean registerCommand(Method method, Executor clazz) {
        CommandInfo ci = CommandInfo.parse(method, clazz);
        if (ci != null) {
            injectPluginCommand(ci);
            Class[] params = method.getParameterTypes();
            Log.d("命令 %s 参数类型: %s", ci.getName(), Arrays.toString(params));
            try {
                Class<? extends CommandSender> sender = params[0];
                cmds.add(ci);
                cmdCache.put(ci.getName(), ci);
                return true;
            } catch (ArrayIndexOutOfBoundsException | ClassCastException ignored) {
            }
            Log.warning(String.format(argumentTypeError, method.getName(), clazz.getClass().getName()));
        }
        return false;
    }

    private void injectPluginCommand(CommandInfo ci) {
        PluginCommand cmd = P.getCommand(ci.getName());
        if (cmd == null) {
            if ((cmd = CommandKit.create(ci.getName(), ci.getAliases().toArray(new String[] {}))) == null) { throw new IllegalStateException("未找到命令 必须在plugin.yml先注册 " + ci.getName() + " 命令!"); }
        }
        cmd.setAliases(ci.getAliases());
        cmd.setExecutor(this);
    }

    /**
     * 检查缓存并获得命令
     *
     * @param cmd
     *            子命令
     * @return 命令信息
     */
    private CommandInfo getByCache(String cmd) {
        if (!cmdCache.containsKey(cmd)) {
            for (CommandInfo cmdinfo : cmds) {
                if (cmdinfo.isValid(cmd)) {
                    cmdCache.put(cmd, cmdinfo);
                    break;
                }
            }
            cmdCache.put(cmd, null);
        }
        return cmdCache.get(cmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1 && args[0].equalsIgnoreCase("help")) {
            help.send(sender, label, args);
            return true;
        }
        CommandInfo manager = getByCache(label);
        return manager != null && manager.execute(sender, label, args);
    }
}
