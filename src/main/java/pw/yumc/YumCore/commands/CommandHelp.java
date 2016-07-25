package pw.yumc.YumCore.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.commands.annotation.Help;

/**
 * 命令帮助生成类
 *
 * @since 2016年7月23日 上午10:12:32
 * @author 喵♂呜
 */
public class CommandHelp {
    /**
     * 插件实例
     */
    Plugin plugin = P.instance;
    /**
     * 消息配置
     */
    String prefix = String.format("§6[§b%s§6] ", P.instance.getName());
    String commandNotFound = prefix + "§c当前插件未注册任何子命令!";
    String pageNotFound = prefix + "§c不存在的帮助页面 §b请输入 /%s help §e1-%s";
    String helpTitle = String.format("§6========= %s §6插件帮助列表=========", prefix);
    String helpBody = "§6/%1$s §a%2$s §e%3$s §6- §b%4$s";
    String helpFooter = "§6查看更多的帮助页面 §b请输入 /%s help §e1-%s";
    /**
     * 已排序的命令列表
     */
    List<CommandInfo> cmdlist;
    /**
     * 帮助页面数量
     */
    private final int HELPPAGECOUNT;
    /**
     * 帮助页面每页行数
     */
    private final int LINES_PER_PAGE = 7;
    /**
     * 帮助列表缓存
     */
    private final Map<String, String[]> cacheHelp = new HashMap<>();

    public CommandHelp(final Collection<? extends CommandInfo> list) {
        cmdlist = new LinkedList<>(list);
        Collections.sort(cmdlist, new CommandComparator());
        this.HELPPAGECOUNT = (int) Math.ceil((double) cmdlist.size() / LINES_PER_PAGE);
    }

    /**
     * 发送帮助
     *
     * @param ca
     *            命令参数
     */
    public void send(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (this.HELPPAGECOUNT == 0) {
            sender.sendMessage(commandNotFound);
            return;
        }
        int page = 1;
        try {
            page = Integer.parseInt(args[1]);
            page = page == 0 ? 1 : page;
        } catch (final Exception e) {
        }
        final String helpkey = label + page;
        if (!cacheHelp.containsKey(helpkey)) {
            final List<String> helpList = new LinkedList<>();
            if (page > this.HELPPAGECOUNT || page < 1) {
                // 帮助页面不存在
                helpList.add(String.format(commandNotFound, HELPPAGECOUNT));
            } else {
                // 帮助标题
                helpList.add(helpTitle);
                final int start = this.LINES_PER_PAGE * (page - 1);
                final int end = start + this.LINES_PER_PAGE;
                for (int i = start; i < end; i++) {
                    if (this.cmdlist.size() > i) {
                        final CommandInfo ci = cmdlist.get(i);
                        final String aliases = Arrays.toString(ci.getCommand().aliases());
                        final String cmd = ci.getName() + (aliases.length() == 2 ? "" : "§7" + aliases);
                        final Help help = ci.getHelp();
                        // 帮助列表
                        helpList.add(String.format(helpBody, label, cmd, help.possibleArguments(), help.value()));
                    }
                }
            }
            // 帮助结尾
            helpList.add(String.format(helpFooter, label, HELPPAGECOUNT));
            cacheHelp.put(helpkey, helpList.toArray(new String[0]));
        }
        sender.sendMessage(cacheHelp.get(helpkey));
    }

    /**
     * 命令排序比较器
     *
     * @since 2016年7月23日 下午4:17:18
     * @author 喵♂呜
     */
    static class CommandComparator implements Comparator<CommandInfo> {
        @Override
        public int compare(final CommandInfo o1, final CommandInfo o2) {
            if (o1.getSort() > o2.getSort()) {
                return 1;
            } else if (o1.getSort() == o2.getSort()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
