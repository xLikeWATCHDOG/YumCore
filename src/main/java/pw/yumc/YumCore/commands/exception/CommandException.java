package pw.yumc.YumCore.commands.exception;

/**
 * 命令执行异常类
 *
 * @since 2016年7月23日 上午10:50:23
 * @author 喵♂呜
 */
public class CommandException extends RuntimeException {

    public CommandException(final Exception e) {
        super(e);
    }

    public CommandException(final String string) {
        super(string);
    }

    public CommandException(final String string, final Exception e) {
        super(string, e);
    }

}
