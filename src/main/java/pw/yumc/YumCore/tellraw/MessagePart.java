package pw.yumc.YumCore.tellraw;

/**
 * Tellraw消息块
 *
 * @since 2016年8月10日 下午7:10:36
 * @author 喵♂呜
 */
public class MessagePart {
    /**
     * 消息文本
     */
    public String text = "";
    /**
     * 点击操作
     */
    public String clickActionName;
    /**
     * 点击数据
     */
    public String clickActionData;
    /**
     * 悬浮操作
     */
    public String hoverActionName;
    /**
     */
    public String hoverActionData;

    public MessagePart() {
        this("");
    }

    public MessagePart(final String text) {
        this.text = text;
    }

    public boolean hasText() {
        return text != null && !text.isEmpty();
    }

    // {
    // "text":"TestClick",
    // "clickEvent":{"action":"run_command","value":"yum list"},
    // "hoverEvent":{"action":"show_text","value":{"text":"§a点击查看插件列表"}}
    // }
    public void writeJson(final StringBuffer str) {
        str.append("{");
        str.append("\"text\":\"" + text + "\"");
        if (clickActionName != null) {
            str.append(",");
            str.append(String.format("\"clickEvent\":{\"action\":\"%s\",\"value\":\"%s\"}", clickActionName, clickActionData));
        }
        if (hoverActionName != null) {
            str.append(",");
            str.append(String.format("\"hoverEvent\":{\"action\":\"%s\",\"value\":{\"text\":\"%s\"}", clickActionName, clickActionData));
        }
        str.append("}");
    }
}
