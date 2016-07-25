package pw.yumc.YumCore.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.bukkit.compatible.C;
import pw.yumc.YumCore.commands.api.CommandExecutor;

/**
 * 命令管理类
 *
 * @since 2016年7月23日 上午9:06:03
 * @author 喵♂呜
 */
public class CommandManager implements TabExecutor {
    private final String argumentTypeError = "注解命令方法 %s 位于 %s 的参数错误 应只有 CommandArgument 参数!";
    private final String returnTypeError = "注解命令补全 %s 位于 %s 的返回值错误 应实现 List 接口!";
    /**
     * 插件实例类
     */
    JavaPlugin plugin = P.instance;
    /**
     * 命令列表
     */
    Set<CommandInfo> cmdlist = new HashSet<>();
    /**
     * Tab列表
     */
    Set<TabInfo> tablist = new HashSet<>();
    /**
     * 命令缓存列表
     */
    Map<String, CommandInfo> cmdcache = new HashMap<>();
    /**
     * 命令帮助
     */
    CommandHelp help = new CommandHelp(cmdlist);

    /**
     * 插件命令
     */
    PluginCommand cmd;

    /**
     * 命令管理器
     *
     * @param name
     *            注册的命令
     */
    public CommandManager(final String name) {
        cmd = plugin.getCommand(name);
        if (cmd == null) {
            throw new IllegalStateException("未找到命令 必须在plugin.yml先注册 " + name + " 命令!");
        }
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    /**
     * 命令管理器
     *
     * @param name
     *            注册的命令
     * @param executor
     *            命令执行类
     */
    public CommandManager(final String name, final CommandExecutor executor) {
        this(name);
        register(executor);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            help.send(sender, command, label, args);
            return true;
        }
        final String subcmd = args[0].toLowerCase();
        if (subcmd.equalsIgnoreCase("help")) {
            help.send(sender, command, label, args);
            return true;
        }
        final String[] subargs = moveStrings(args, 1);
        if (!cmdcache.containsKey(label)) {
            for (final CommandInfo cmdinfo : cmdlist) {
                if (cmdinfo.isValid(subcmd)) {
                    cmdcache.put(subcmd, cmdinfo);
                    break;
                }
            }
            if (!cmdcache.containsKey(subcmd)) {
                cmdcache.put(subcmd, CommandInfo.Unknow);
            }
        }
        return cmdcache.get(subcmd).execute(sender, command, label, subargs);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final List<String> completions = new ArrayList<>();
        final String token = args[args.length - 1];
        if (args.length == 1) {
            final Set<String> commands = this.cmdcache.keySet();
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length >= 2) {
            for (final TabInfo tab : tablist) {
                StringUtil.copyPartialMatches(token, tab.execute(sender, command, token, args), completions);
            }
            StringUtil.copyPartialMatches(token, getPlayerTabComplete(sender, command, alias, args), completions);
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    /**
     * 通过注解读取命令并注册
     *
     * @param clazz
     *            子命令处理类
     */
    public void register(final CommandExecutor clazz) {
        final Method[] methods = clazz.getClass().getDeclaredMethods();
        for (final Method method : methods) {
            if (registerCommand(method, clazz)) {
                continue;
            }
            registerTab(method, clazz);
        }
        help = new CommandHelp(cmdlist);
    }

    /**
     * 获取玩家命令补全
     *
     * @param sender
     *            命令发送者
     * @param command
     *            命令
     * @param alias
     *            别名
     * @param args
     *            数组
     * @return 在线玩家数组
     */
    private List<String> getPlayerTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final String lastWord = args[args.length - 1];
        final Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        final ArrayList<String> matchedPlayers = new ArrayList<>();
        for (final Player player : C.Player.getOnlinePlayers()) {
            final String name = player.getName();
            if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, lastWord)) {
                matchedPlayers.add(name);
            }
        }
        return matchedPlayers;
    }

    /**
     * 转移数组
     *
     * @param args
     *            原数组
     * @param start
     *            数组开始位置
     * @return 转移后的数组字符串
     */
    private String[] moveStrings(final String[] args, final int start) {
        final String[] ret = new String[args.length - start];
        System.arraycopy(args, start, ret, 0, ret.length);
        return ret;
    }

    /**
     * 注册命令
     *
     * @param method
     *            方法
     * @param clazz
     *            调用对象
     * @return 是否成功
     */
    private boolean registerCommand(final Method method, final CommandExecutor clazz) {
        final CommandInfo ci = CommandInfo.parse(method, clazz);
        if (ci != null) {
            final Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0].equals(CommandArgument.class)) {
                cmdlist.add(ci);
                cmdcache.put(ci.getName(), ci);
                return true;
            }
            Log.warning(String.format(argumentTypeError, method.getName(), clazz.getClass().getName()));
        }
        return false;
    }

    /**
     * 注册Tab补全
     *
     * @param method
     *            方法
     * @param clazz
     *            调用对象
     * @return 是否成功
     */
    private boolean registerTab(final Method method, final CommandExecutor clazz) {
        final TabInfo ti = TabInfo.parse(method, clazz);
        if (ti != null) {
            if (method.getReturnType().equals(List.class)) {
                tablist.add(ti);
                return true;
            }
            Log.warning(String.format(returnTypeError, method.getName(), clazz.getClass().getName()));
        }
        return false;
    }
}
