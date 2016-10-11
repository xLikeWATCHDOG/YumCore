package pw.yumc.YumCore.commands.info;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.CommandParse;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Cmd.Executor;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.Sort;
import pw.yumc.YumCore.commands.exception.CommandArgumentException;
import pw.yumc.YumCore.commands.exception.CommandException;
import pw.yumc.YumCore.commands.exception.CommandParseException;

/**
 * 命令信息存储类
 *
 * @since 2016年7月23日 上午9:56:42
 * @author 喵♂呜
 */
public class CommandInfo {
    public static final CommandInfo Unknow = new CommandInfo();
    private static final String onlyExecutor = "§c当前命令仅允许 §b%s §c执行!";
    private static final String losePerm = "§c你需要有 %s 的权限才能执行此命令!";
    private static final String argErr = "§c参数错误: §4%s";
    private static final String cmdErr = "§6错误原因: §4命令参数不正确!";
    private static final String cmdUse = "§6使用方法: §e/%s %s %s";
    private static final String cmdDes = "§6命令描述: §3%s";
    private final Object origin;
    private final Method method;
    private final String name;
    private final List<String> aliases;
    private final List<Executor> executors;
    private final String executorStr;
    private final boolean async;
    private final Cmd command;
    private final Help help;
    private final int sort;
    private final CommandParse parse;

    public CommandInfo(final Method method, final Object origin, final Cmd command, final Help help, final boolean async, final int sort, final CommandParse parse) {
        this.method = method;
        this.origin = origin;
        this.name = "".equals(command.value()) ? method.getName().toLowerCase() : command.value();
        this.aliases = Arrays.asList(command.aliases());
        this.executors = Arrays.asList(command.executor());
        this.executorStr = eS(executors);
        this.command = command;
        this.help = help;
        this.async = async;
        this.sort = sort;
        this.parse = parse;
    }

    private CommandInfo() {
        this.method = null;
        this.origin = null;
        this.name = "unknow";
        this.aliases = null;
        this.executors = null;
        this.executorStr = null;
        this.command = null;
        this.help = null;
        this.async = false;
        this.sort = 0;
        this.parse = null;
    }

    /**
     * 解析CommandInfo
     *
     * @param method
     *            方法
     * @param origin
     *            源对象
     * @return {@link CommandInfo}
     */
    public static CommandInfo parse(final Method method, final Object origin) {
        final Cmd command = method.getAnnotation(Cmd.class);
        if (command != null) {
            final Help help = method.getAnnotation(Help.class);
            final Async async = method.getAnnotation(Async.class);
            final Sort sort = method.getAnnotation(Sort.class);
            final CommandParse cp = CommandParse.get(method);
            return new CommandInfo(method, origin, command, help != null ? help : Help.DEFAULT, async != null, sort != null ? sort.value() : 50, cp);
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CommandInfo) {
            return name.equalsIgnoreCase(((CommandInfo) obj).getName());
        }
        return super.equals(obj);
    }

    /**
     * 执行命令
     *
     * @param cmdArgs
     *            命令参数
     * @return 是否执行成功
     */
    public boolean execute(final CommandArgument cmdArgs) {
        if (method == null) {
            return false;
        }
        if (check(cmdArgs)) {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(origin, parse.parse(cmdArgs));
                    } catch (final CommandParseException | CommandArgumentException e) {
                        Log.toSender(cmdArgs.getSender(), argErr, e.getMessage());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new CommandException(e);
                    }
                }
            };
            if (async) {
                Bukkit.getScheduler().runTaskAsynchronously(P.instance, runnable);
            } else {
                runnable.run();
            }
        }
        return true;
    }

    /**
     * @return 命令注解
     */
    public Cmd getCommand() {
        return command;
    }

    /**
     * @return 帮助注解
     */
    public Help getHelp() {
        return help;
    }

    /**
     * @return 命令名称
     */
    public String getName() {
        return name;
    }

    /**
     * @return 命令排序
     */
    public int getSort() {
        return sort;
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode() + method.hashCode() + origin.hashCode();
    }

    /**
     * @return 是否为异步命令
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * 验证命令是否匹配
     *
     * @param cmd
     *            需验证命令
     * @return 是否匹配
     */
    public boolean isValid(final String cmd) {
        return name.equalsIgnoreCase(cmd) || aliases.contains(cmd);
    }

    /**
     * 检查命令
     *
     * @param info
     *            命令信息
     * @return 是否验证通过
     */
    private boolean check(final CommandArgument cmdArgs) {
        final CommandSender sender = cmdArgs.getSender();
        return checkSender(sender) && checkArgs(sender, cmdArgs) && checkPerm(sender);
    }

    private boolean checkArgs(final CommandSender sender, final CommandArgument cmdArgs) {
        if (cmdArgs.getArgs().length < command.minimumArguments()) {
            Log.toSender(sender, cmdErr);
            Log.toSender(sender, String.format(cmdUse, cmdArgs.getAlias(), getName(), help.possibleArguments()));
            Log.toSender(sender, String.format(cmdDes, help.value()));
            return false;
        }
        return true;
    }

    private boolean checkPerm(final CommandSender sender) {
        final String perm = command.permission();
        if (perm != null && !"".equals(perm) && !sender.hasPermission(perm)) {
            Log.toSender(sender, String.format(losePerm, perm));
            return false;
        }
        return true;
    }

    private boolean checkSender(final CommandSender sender) {
        if (!executors.contains(Executor.ALL) && !executors.contains(Executor.valueOf(sender))) {
            Log.toSender(sender, String.format(onlyExecutor, executorStr));
            return false;
        }
        return true;
    }

    private String eS(final List<Executor> executors) {
        final StringBuilder str = new StringBuilder();
        for (final Executor executor : executors) {
            str.append(executor.getName());
            str.append(", ");
        }
        return str.toString().substring(0, str.length() - 2);
    }
}
