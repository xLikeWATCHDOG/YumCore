package pw.yumc.YumCore.commands.exception;

/**
 * 命令参数解析异常
 * 
 * @author 喵♂呜
 * @since 2016年10月5日 下午5:15:43
 */
public class CommandParseException extends CommandException {

    public CommandParseException(Exception e) {
        super(e);
    }

    public CommandParseException(String string) {
        super(string);
    }

    public CommandParseException(String string, Exception e) {
        super(string, e);
    }
}
