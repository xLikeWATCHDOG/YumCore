package pw.yumc.YumCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * 子命令参数类
 *
 * @since 2015年8月22日上午8:29:44
 * @author 喵♂呜
 */
public class CommandArgument {
    private CommandSender sender;
    private Command command;
    private String alias;
    private String[] args;

    public CommandArgument(CommandSender sender, Command command, String alias, String[] args) {
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
     * @return 命令发送者
     */
    public CommandSender getSender() {
        return sender;
    }
}
