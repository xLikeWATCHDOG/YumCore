package pw.yumc.YumCore.commands.info;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.annotation.Tab;
import pw.yumc.YumCore.commands.exception.CommandException;

/**
 * Tab补全
 *
 * @since 2016年7月23日 上午9:56:42
 * @author 喵♂呜
 */
public class CommandTabInfo {
    private final Object origin;
    private final Method method;

    public CommandTabInfo(final Method method, final Object origin) {
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
    public static CommandTabInfo parse(final Method method, final Object origin) {
        final Tab tab = method.getAnnotation(Tab.class);
        if (tab != null) {
            return new CommandTabInfo(method, origin);
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CommandTabInfo) {
            return method.equals(((CommandTabInfo) obj).getMethod());
        }
        return super.equals(obj);
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
    public List<String> execute(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        final CommandArgument cmdArgs = new CommandArgument(sender, command, label, args);
        try {
            return (List<String>) method.invoke(origin, cmdArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CommandException("调用Tab自动补全发生错误 请反馈给开发者 " + Arrays.toString(P.instance.getDescription().getAuthors().toArray()) + " !", e);
        }
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public int hashCode() {
        return method.hashCode() + origin.hashCode();
    }
}
