package pw.yumc.YumCore.commands;

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
import pw.yumc.YumCore.commands.exception.ArgumentException;
import pw.yumc.YumCore.commands.exception.CommandException;
import pw.yumc.YumCore.commands.exception.PermissionException;
import pw.yumc.YumCore.commands.exception.SenderException;
import pw.yumc.YumCore.commands.info.CommandInfo;
import pw.yumc.YumCore.commands.info.CommandTabInfo;
import pw.yumc.YumCore.commands.interfaces.ErrorHanlder;
import pw.yumc.YumCore.commands.interfaces.Executor;
import pw.yumc.YumCore.commands.interfaces.HelpGenerator;
import pw.yumc.YumCore.commands.interfaces.HelpParse;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 子命令管理类
 *
 * @author 喵♂呜
 * @since 2016年7月23日 上午9:06:03
 */
public class CommandSub implements TabExecutor {
    private static String argumentTypeError = "注解命令方法 %s 位于 %s 的参数错误 第一个参数应实现 CommandSender 接口!";
    private static String returnTypeError = "注解命令补全 %s 位于 %s 的返回值错误 应实现 List 接口!";
    private static String onlyExecutor = "§c当前命令仅允许 §b%s §c执行!";
    private static String losePerm = "§c你需要有 %s 的权限才能执行此命令!";
    private static String cmdErr = "§6错误原因: §4命令参数不正确!";
    private static String cmdUse = "§6使用方法: §e/%s %s%s";
    private static String cmdDes = "§6命令描述: §3%s";

    /**
     * 命令帮助
     */
    private CommandHelp help;
    /**
     * 插件命令
     */
    private PluginCommand cmd;
    /**
     * 命令错误处理
     */
    private ErrorHanlder commandErrorHanlder = new ErrorHanlder() {
        @Override
        public void error(CommandException e, CommandSender sender, CommandInfo info, String label, String[] args) {
            if (e instanceof SenderException) {
                Log.sender(sender, onlyExecutor, info.getExecutorStr());
            } else if (e instanceof PermissionException) {
                Log.sender(sender, losePerm, info.getCommand().permission());
            } else if (e instanceof ArgumentException) {
                Log.sender(sender, cmdErr);
                Log.sender(sender, cmdUse, label, info.isMain() ? "" : info.getName() + " ", info.getHelp().possibleArguments());
                Log.sender(sender, cmdDes, info.getHelp().value());
            }
        }
    };
    /**
     * 插件实例类
     */
    private static JavaPlugin plugin = P.instance;
    /**
     * 默认命令
     */
    private CommandInfo defCmd = null;
    /**
     * 命令列表
     */
    private Set<CommandInfo> cmds = new HashSet<>();
    /**
     * Tab列表
     */
    private Set<CommandTabInfo> tabs = new HashSet<>();
    /**
     * 命令缓存列表
     */
    private Map<String, CommandInfo> cmdCache = new HashMap<>();
    /**
     * 命令名称缓存
     */
    private List<String> cmdNameCache = new ArrayList<>();

    /**
     * 命令管理器
     *
     * @param name
     *            注册的命令
     */
    public CommandSub(String name) {
        cmd = plugin.getCommand(name);
        if (cmd == null) {
            if ((cmd = CommandKit.create(name)) == null) { throw new IllegalStateException("未找到命令 必须在plugin.yml先注册 " + name + " 命令!"); }
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
    public CommandSub(String name, Executor... executor) {
        this(name);
        register(executor);
    }

    /**
     * 构建命令列表缓存
     */
    private void buildCmdNameCache() {
        cmdNameCache.clear();
        for (CommandInfo cmd : cmds) {
            cmdNameCache.add(cmd.getName());
            cmdNameCache.addAll(Arrays.asList(cmd.getCommand().aliases()));
        }
        cmdNameCache.add("help");
    }

    /**
     * 检查缓存并获得命令
     *
     * @param subcmd
     *            子命令
     * @return 命令信息
     */
    private CommandInfo getByCache(String subcmd) {
        if (!cmdCache.containsKey(subcmd)) {
            for (CommandInfo cmdinfo : cmds) {
                if (cmdinfo.isValid(subcmd)) {
                    cmdCache.put(subcmd, cmdinfo);
                    break;
                }
            }
            if (!cmdCache.containsKey(subcmd)) {
                cmdCache.put(subcmd, CommandInfo.Unknow);
            }
        }
        return cmdCache.get(subcmd);
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
    private List<String> getPlayerTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String lastWord = args[args.length - 1];
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        List<String> matchedPlayers = new ArrayList<>();
        for (Player player : C.Player.getOnlinePlayers()) {
            String name = player.getName();
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
     * @return 转移后的数组字符串
     */
    private String[] moveStrings(String[] args) {
        String[] ret = new String[args.length - 1];
        System.arraycopy(args, 1, ret, 0, ret.length);
        return ret;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (defCmd != null) { return defCmd.execute(sender, label, args); }
            return help.send(sender, label, args);
        }
        String subcmd = args[0].toLowerCase();
        if (subcmd.equalsIgnoreCase("help")) { return help.send(sender, label, args); }
        CommandInfo cmd = getByCache(subcmd);
        String[] subargs = args;
        if (cmd.equals(CommandInfo.Unknow) && defCmd != null) {
            cmd = defCmd;
        } else {
            subargs = moveStrings(args);
        }
        try {
            return cmd.execute(sender, label, subargs);
        } catch (CommandException e) {
            commandErrorHanlder.error(e, sender, cmd, label, subargs);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String token = args[args.length - 1];
        if (args.length == 1) {
            StringUtil.copyPartialMatches(token, cmdNameCache, completions);
        }
        for (CommandTabInfo tab : tabs) {
            StringUtil.copyPartialMatches(token, tab.execute(sender, token, args), completions);
        }
        StringUtil.copyPartialMatches(token, getPlayerTabComplete(sender, command, alias, args), completions);
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    /**
     * 通过注解读取命令并注册
     *
     * @param clazzs
     *            子命令处理类
     * @return {@link CommandSub}
     */
    public CommandSub register(Executor... clazzs) {
        for (Executor clazz : clazzs) {
            Method[] methods = clazz.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (registerCommand(method, clazz)) {
                    continue;
                }
                registerTab(method, clazz);
            }
        }
        help = new CommandHelp(defCmd, cmds);
        buildCmdNameCache();
        return this;
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
    private boolean registerCommand(Method method, Executor clazz) {
        CommandInfo ci = CommandInfo.parse(method, clazz);
        if (ci != null) {
            Class[] params = method.getParameterTypes();
            Log.d("注册命令 %s 参数类型: %s", ci.getName(), Arrays.toString(params));
            try {
                Class<? extends CommandSender> sender = params[0];
                // 用于消除unuse警告
                if (!sender.getName().isEmpty() && method.getReturnType() == boolean.class) {
                    defCmd = ci;
                } else {
                    cmds.add(ci);
                    cmdCache.put(ci.getName(), ci);
                }
                return true;
            } catch (ArrayIndexOutOfBoundsException | ClassCastException ignored) {
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
    private boolean registerTab(Method method, Executor clazz) {
        CommandTabInfo ti = CommandTabInfo.parse(method, clazz);
        if (ti != null) {
            if (method.getReturnType().equals(List.class)) {
                tabs.add(ti);
                return true;
            }
            Log.warning(String.format(returnTypeError, method.getName(), clazz.getClass().getName()));
        }
        return false;
    }

    /**
     * 设置命令错误处理器
     * 
     * @param commandErrorHanlder
     *            命令错误处理器
     * @return {@link CommandSub}
     */
    public CommandSub setCommandErrorHanlder(ErrorHanlder commandErrorHanlder) {
        this.commandErrorHanlder = commandErrorHanlder;
        return this;
    }

    /**
     * 设置帮助生成器
     *
     * @param helpGenerator
     *            帮助生成器
     * @return {@link CommandSub}
     */
    public CommandSub setHelpGenerator(HelpGenerator helpGenerator) {
        help.setHelpGenerator(helpGenerator);
        return this;
    }

    /**
     * 设置帮助解析器
     *
     * @param helpParse
     *            帮助解析器
     * @return {@link CommandSub}
     */
    public CommandSub setHelpParse(HelpParse helpParse) {
        if (help.getHelpGenerator() instanceof CommandHelp.DefaultHelpGenerator) {
            ((CommandHelp.DefaultHelpGenerator) help.getHelpGenerator()).setHelpParse(helpParse);
        } else {
            Log.w("已设置自定义帮助生成器 解析器设置将不会生效!");
        }
        return this;
    }
}