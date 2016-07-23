package pw.yumc.YumCore.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.Sort;
import pw.yumc.YumCore.commands.exception.CommandException;

/**
 * 命令信息存储类
 *
 * @since 2016年7月23日 上午9:56:42
 * @author 喵♂呜
 */
public class CommandInfo {
    public static CommandInfo Unknow = new CommandInfo();
    private final Object origin;
    private final Method method;
    private final String name;
    private final List<String> aliases;
    private final boolean async;
    private final Cmd command;
    private final Help help;
    private final int sort;

    public CommandInfo(final Method method, final Object origin, final Cmd command, final Help help, final boolean async, final int sort) {
        this.method = method;
        this.origin = origin;
        this.name = "".equals(command.name()) ? method.getName().toLowerCase() : command.name();
        this.aliases = Arrays.asList(command.aliases());
        this.command = command;
        this.help = help;
        this.async = async;
        this.sort = sort;
    }

    private CommandInfo() {
        this.method = null;
        this.origin = null;
        this.name = "unknow";
        this.aliases = null;
        this.command = null;
        this.help = null;
        this.async = false;
        this.sort = 0;
    }

    public static CommandInfo parse(final Method method, final Object origin) {
        final Class<?> clazz = method.getClass();
        final Cmd command = clazz.getAnnotation(Cmd.class);
        if (command != null) {
            final Help help = clazz.getAnnotation(Help.class);
            final Async async = clazz.getAnnotation(Async.class);
            final Sort sort = clazz.getAnnotation(Sort.class);
            return new CommandInfo(method, origin, command, help != null ? help : Help.DEFAULT, async != null, sort != null ? sort.sort() : 50);
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CommandInfo) {
            return name.equalsIgnoreCase(((CommandInfo) obj).getName());
        }
        return super.equals(obj);
    }

    public boolean execute(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (method == null) {
            return false;
        }
        final CommandArgument cmdArgs = new CommandArgument(sender, command, label, args);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        cmdArgs.check(CommandInfo.this);
                    } catch (final CommandException e) {
                        sender.sendMessage(e.getMessage());
                        return;
                    }
                    method.invoke(origin, cmdArgs);
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
        return true;
    }

    public Cmd getCommand() {
        return command;
    }

    public Help getHelp() {
        return help;
    }

    public String getName() {
        return name;
    }

    public int getSort() {
        return sort;
    }

    public boolean isAsync() {
        return async;
    }

    public boolean isValid(final String cmd) {
        return name.equalsIgnoreCase(cmd) || aliases.contains(cmd);
    }
}
