package pw.yumc.YumCore.tellraw;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * TellRaw简易处理类
 *
 * @since 2016年8月10日 下午7:10:08
 * @author 喵♂呜
 */
public class Tellraw {
    List<MessagePart> messageParts = new ArrayList<>();

    public Tellraw command(final String command) {
        onClick("run_command", command);
        return this;
    }

    public void send(final CommandSender sender) {
        if (sender instanceof Player) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + toJsonString());
        } else {
            sender.sendMessage(toOldMessageFormat());
        }
    }

    public Tellraw suggest(final String command) {
        onClick("suggest_command", command);
        return this;
    }

    /**
     * 结束上一串消息 开始下一串数据
     *
     * @param part
     *            下一段内容
     * @return {@link Tellraw}
     */
    public Tellraw then(final MessagePart part) {
        if (!latest().hasText()) {
            throw new IllegalStateException("previous message part has no text");
        }
        messageParts.add(part);
        return this;
    }

    /**
     * 结束上一串消息 开始下一串数据
     *
     * @param text
     *            新的文本
     * @return {@link Tellraw}
     */
    public Tellraw then(final String text) {
        then(new MessagePart(text));
        return this;
    }

    public Tellraw tip(final String text) {
        onHover("show_text", text);
        return this;
    }

    public String toJsonString() {
        final StringBuffer msg = new StringBuffer();
        msg.append("[\"\"");
        for (final MessagePart messagePart : messageParts) {
            msg.append(",");
            messagePart.writeJson(msg);
        }
        msg.append("]");
        return null;
    }

    /**
     * 将此消息转换为具有有限格式的人可读字符串。
     * 此方法用于发送此消息给没有JSON格式支持客户端。
     * <p>
     * 序列化每个消息部分（每个部分都需要分别序列化）：
     * <ol>
     * <li>消息串的颜色.</li>
     * <li>消息串的样式.</li>
     * <li>消息串的文本.</li>
     * </ol>
     * 这个方法会丢失点击操作和悬浮操作 所以仅用于最后的手段
     * </p>
     * <p>
     * 颜色和格式可以从返回的字符串中删除 通过{@link ChatColor#stripColor(String)}.
     * </p>
     *
     * @return 发送给老版本客户端以及控制台。
     */
    public String toOldMessageFormat() {
        final StringBuilder result = new StringBuilder();
        for (final MessagePart part : messageParts) {
            result.append(part.text);
        }
        return result.toString();
    }

    /**
     * 获得最后一个操作串
     *
     * @return 最后一个操作的消息串
     */
    private MessagePart latest() {
        return messageParts.get(messageParts.size() - 1);
    }

    /**
     * 添加点击操作
     *
     * @param name
     *            点击名称
     * @param data
     *            点击操作
     */
    private void onClick(final String name, final String data) {
        final MessagePart latest = latest();
        latest.clickActionName = name;
        latest.clickActionData = data;
    }

    /**
     * 添加显示操作
     *
     * @param name
     *            悬浮显示
     * @param data
     *            显示内容
     */
    private void onHover(final String name, final String data) {
        final MessagePart latest = latest();
        latest.hoverActionName = name;
        latest.hoverActionData = data;
    }
}