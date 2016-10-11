package pw.yumc.YumCore.commands.exception;

/**
 * 命令参数解析异常
 * 
 * @author 喵♂呜
 * @since 2016年10月5日 下午5:15:43
 */
public class CommandArgumentException extends CommandException {

    public CommandArgumentException(final Exception e) {
        super(e);
    }

    public CommandArgumentException(final String string) {
        super(string);
    }

    public CommandArgumentException(final String string, final Exception e) {
        super(string, e);
    }
}
