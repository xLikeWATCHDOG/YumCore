package pw.yumc.YumCore.commands;

/**
 * 命令解析接口
 *
 * @author 喵♂呜
 * @since 2016年9月14日 上午12:39:26
 */
public interface CommandHelpParse {
    /**
     * 解析命令帮助
     *
     * @param str
     *            参数
     * @return 命令帮助
     */
    public String parse(String str);
}
