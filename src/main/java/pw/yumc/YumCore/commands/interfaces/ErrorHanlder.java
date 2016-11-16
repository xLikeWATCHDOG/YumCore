package pw.yumc.YumCore.commands.interfaces;

import org.bukkit.command.CommandSender;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.exception.CommandException;
import pw.yumc.YumCore.commands.info.CommandInfo;

/**
 * 命令错误处理
 *
 * @since 2016年7月23日 上午9:55:51
 * @author 喵♂呜
 */
public interface ErrorHanlder {
    void error(CommandException e, CommandSender sender, CommandInfo info, CommandArgument args);
}
