package pw.yumc.YumCore.commands.info;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 命令信息存储类
 *
 * @author 喵♂呜
 * @since 2016年7月23日 上午9:56:42
 */
public class CommandInfo {
    public static CommandInfo Unknow = new CommandInfo();
    private static String onlyExecutor = "§c当前命令仅允许 §b%s §c执行!";
    private static String losePerm = "§c你需要有 %s 的权限才能执行此命令!";
    private static String argErr = "§c参数错误: §4%s";
    private static String cmdErr = "§6错误原因: §4命令参数不正确!";
    private static String cmdUse = "§6使用方法: §e/%s %s %s";
    private static String cmdDes = "§6命令描述: §3%s";
    private static Help defHelp = new Help() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Help.class;
        }

        @Override
        public String possibleArguments() {
            return "这家伙很懒";
        }

        @Override
        public String value() {
            return "没写帮助信息";
        }
    };
    private Object origin;
    private Method method;
    private String name;
    private List<String> aliases;
    private List<Executor> executors;
    private String executorStr;
    private boolean async;
    private Cmd command;
    private Help help;
    private int sort;
    private CommandParse parse;

    public CommandInfo(Method method, Object origin, Cmd command, Help help, boolean async, int sort, CommandParse parse) {
        this.method = method;
        this.origin = origin;
        this.name = "".equals(command.value()) ? method.getName().toLowerCase() : command.value();
        this.aliases = Arrays.asList(command.aliases());
        this.executors = Arrays.asList(command.executor());
        this.executorStr = eS(executors);
        this.command = command;
        this.help = help != null ? help : defHelp;
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
    public static CommandInfo parse(Method method, Object origin) {
        Cmd command = method.getAnnotation(Cmd.class);
        if (command != null) {
            Help help = method.getAnnotation(Help.class);
            Async async = method.getAnnotation(Async.class);
            Sort sort = method.getAnnotation(Sort.class);
            CommandParse cp = CommandParse.get(method);
            return new CommandInfo(method, origin, command, help, async != null, sort != null ? sort.value() : 50, cp);
        }
        return null;
    }

    /**
     * 执行命令
     *
     * @param cmdArgs
     *            命令参数
     * @return 是否执行成功
     */
    public boolean execute(final CommandArgument cmdArgs) {
        if (method == null) { return false; }
        if (check(cmdArgs)) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(origin, parse.parse(cmdArgs));
                    } catch (CommandParseException | CommandArgumentException e) {
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
    public boolean isValid(String cmd) {
        return name.equalsIgnoreCase(cmd) || aliases.contains(cmd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandInfo that = (CommandInfo) o;
        return Objects.equals(origin, that.origin) && Objects.equals(method, that.method) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, method, name);
    }

    private boolean check(CommandArgument cmdArgs) {
        CommandSender sender = cmdArgs.getSender();
        return checkSender(sender) && checkArgs(sender, cmdArgs) && checkPerm(sender);
    }

    private boolean checkArgs(CommandSender sender, CommandArgument cmdArgs) {
        if (cmdArgs.getArgs().length < command.minimumArguments()) {
            Log.toSender(sender, cmdErr);
            Log.toSender(sender, cmdUse, cmdArgs.getAlias(), getName(), help.possibleArguments());
            Log.toSender(sender, cmdDes, help.value());
            return false;
        }
        return true;
    }

    private boolean checkPerm(CommandSender sender) {
        String perm = command.permission();
        if (!"".equals(perm) && !sender.hasPermission(perm)) {
            Log.toSender(sender, losePerm, perm);
            return false;
        }
        return true;
    }

    private boolean checkSender(CommandSender sender) {
        if (!executors.contains(Executor.ALL) && !executors.contains(Executor.valueOf(sender))) {
            Log.toSender(sender, onlyExecutor, executorStr);
            return false;
        }
        return true;
    }

    private String eS(List<Executor> executors) {
        StringBuilder str = new StringBuilder();
        for (Executor executor : executors) {
            str.append(executor.getName());
            str.append(", ");
        }
        return str.toString().substring(0, str.length() - 2);
    }
}