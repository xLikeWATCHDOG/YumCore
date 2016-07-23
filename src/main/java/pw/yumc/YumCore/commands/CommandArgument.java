package pw.yumc.YumCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pw.yumc.YumCore.commands.exception.IllegalPermissionException;
import pw.yumc.YumCore.commands.exception.IllegalSenderException;

/**
 * 子命令调用事件类
 *
 * @since 2015年8月22日上午8:29:44
 * @author 喵♂呜
 */
public class CommandArgument {
    private final CommandSender sender;
    private final Command command;
    private final String alias;
    private final String[] args;

    public CommandArgument(final CommandSender sender, final Command command, final String alias, final String[] args) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.args = args;
    }

    public boolean check(final CommandInfo info) {
        if (sender instanceof Player) {
            if (info.getCommand().onlyConsoleExecutable()) {
                throw new IllegalSenderException("§c玩家无法使用此命令(§4请使用控制台执行§c)!");
            }
        } else if (info.getCommand().onlyPlayerExecutable()) {
            throw new IllegalSenderException("§c玩家无法使用此命令(§4请使用控制台执行§c)!");
        }
        final String perm = info.getCommand().permission();
        if (perm != null && !sender.hasPermission(perm)) {
            throw new IllegalPermissionException("§c你需要有 " + perm + " 的权限才能执行此命令!");
        }
        return true;
    }

    /**
     * 命令别名
     *
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * 命令参数
     *
     * @return args
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * 命令实体
     *
     * @return command
     */
    public Command getCommand() {
        return command;
    }

    /**
     * 命令发送者
     *
     * @return sender
     */
    public CommandSender getSender() {
        return sender;
    }

}
