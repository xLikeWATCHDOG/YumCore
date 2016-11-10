package pw.yumc.YumCore.commands.info;

import org.bukkit.command.CommandSender;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.annotation.Tab;
import pw.yumc.YumCore.commands.exception.CommandException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Tab补全
 *
 * @since 2016年7月23日 上午9:56:42
 * @author 喵♂呜
 */
public class CommandTabInfo {
    private Object origin;
    private Method method;

    public CommandTabInfo(Method method, Object origin) {
        this.method = method;
        this.origin = origin;
    }

    /**
     * 解析TabInfo
     *
     * @param method
     *            方法
     * @param origin
     *            对象
     * @return {@link CommandTabInfo}
     */
    public static CommandTabInfo parse(Method method, Object origin) {
        Tab tab = method.getAnnotation(Tab.class);
        if (tab != null) { return new CommandTabInfo(method, origin); }
        return null;
    }

    /**
     * 获得补全List
     *
     * @param sender
     *            发送者
     * @param command
     *            命令
     * @param label
     *            命令
     * @param args
     *            参数
     * @return Tab补全信息
     */
    @SuppressWarnings("unchecked")
    public List<String> execute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        CommandArgument cmdArgs = new CommandArgument(sender, command, label, args);
        try {
            return (List<String>) method.invoke(origin, cmdArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CommandException("调用Tab自动补全发生错误 请反馈给开发者 " + Arrays.toString(P.getDescription().getAuthors().toArray()) + " !", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandTabInfo that = (CommandTabInfo) o;
        return Objects.equals(origin, that.origin) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, method);
    }
}
