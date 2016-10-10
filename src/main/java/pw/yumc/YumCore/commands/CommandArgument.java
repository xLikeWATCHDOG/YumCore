package pw.yumc.YumCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 子命令参数类
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

    /**
     * @return 命令别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return 命令参数
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * @return 命令实体
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @return 命令发送者(转换为Player)
     */
    public Player getPlayer() {
        return (Player) sender;
    }

    /**
     * @return 命令发送者
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * @return 命令发送者(自动转换)
     */
    @SuppressWarnings("unchecked")
    public <CS> CS getSenderEx() {
        return (CS) sender;
    }
}
