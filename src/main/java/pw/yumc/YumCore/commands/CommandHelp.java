package pw.yumc.YumCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.info.CommandInfo;
import pw.yumc.YumCore.commands.interfaces.HelpGenerator;
import pw.yumc.YumCore.commands.interfaces.HelpParse;

import java.util.*;

/**
 * 命令帮助生成类
 *
 * @since 2016年7月23日 上午10:12:32
 * @author 喵♂呜
 */
public class CommandHelp {
    private static String commandNotFound = "§c当前插件未注册默认命令以及子命令!";
    /**
     * 默认命令
     */
    private CommandInfo defCmd;
    /**
     * 已排序的命令列表
     */
    private List<CommandInfo> cmdlist;
    /**
     * 帮助页生成
     */
    private HelpGenerator helpGenerator = new DefaultHelpGenerator();
    /**
     * 帮助列表缓存
     */
    private Map<String, String[]> cacheHelp = new HashMap<>();
    /**
     * 帮助页面每页行数
     */
    private static int LINES_PER_PAGE = 7;
    /**
     * 帮助页面数量
     */
    private int HELPPAGECOUNT;

    /**
     * 命令帮助
     *
     * @param list
     *            子命令列表
     */
    public CommandHelp(Collection<? extends CommandInfo> list) {
        this(null, list);
    }

    /**
     * 命令帮助
     *
     * @param defCmd
     *            默认命令
     * @param list
     *            子命令列表
     */
    public CommandHelp(CommandInfo defCmd, Collection<? extends CommandInfo> list) {
        this.defCmd = defCmd;
        cmdlist = new LinkedList<>(list);
        Collections.sort(cmdlist, new CommandNameComparator());
        Collections.sort(cmdlist, new CommandComparator());
        HELPPAGECOUNT = (int) Math.ceil((double) cmdlist.size() / LINES_PER_PAGE);
    }

    /**
     * 发送帮助
     *
     * @param sender
     *            命令发送者
     * @param command
     *            命令
     * @param label
     *            标签
     * @param args
     *            参数
     * @return 是否发送成功
     */
    public boolean send(CommandSender sender, Command command, String label, String[] args) {
        if (this.HELPPAGECOUNT == 0) {
            Log.sender(sender, commandNotFound);
            return true;
        }
        int page = 1;
        try {
            page = Integer.parseInt(args[1]);
            page = page == 0 ? 1 : page;
        } catch (Exception ignored) {
        }
        String helpkey = label + page;
        if (!cacheHelp.containsKey(helpkey)) {
            if (page > HELPPAGECOUNT || page < 1) {
                // 帮助页面不存在 
                cacheHelp.put(helpkey, new String[] { helpGenerator.notFound(label, HELPPAGECOUNT) });
            } else {
                List<String> helpList = new LinkedList<>();
                // 帮助标题
                helpList.add(helpGenerator.title());
                if (page == 1 && defCmd != null) {
                    helpList.add(helpGenerator.body(label, defCmd));
                }
                int start = LINES_PER_PAGE * (page - 1);
                int end = start + LINES_PER_PAGE;
                for (int i = start; i < end; i++) {
                    if (cmdlist.size() > i) {
                        // 帮助列表
                        helpList.add(helpGenerator.body(label, cmdlist.get(i)));
                    }
                }
                // 帮助结尾
                helpList.add(helpGenerator.foot(label, HELPPAGECOUNT));
                cacheHelp.put(helpkey, helpList.toArray(new String[] {}));
            }
        }
        sender.sendMessage(cacheHelp.get(helpkey));
        return true;
    }

    /**
     * 设置帮助生成器
     * 
     * @param helpGenerator
     *            帮助生成器
     */
    public void setHelpGenerator(HelpGenerator helpGenerator) {
        this.helpGenerator = helpGenerator;
    }

    /**
     * @return 命令帮助生成器
     */
    public HelpGenerator getHelpGenerator() {
        return helpGenerator;
    }

    /**
     * 命令名称比较器
     *
     * @since 2016年7月23日 下午4:17:18
     * @author 喵♂呜
     */
    static class CommandNameComparator implements Comparator<CommandInfo> {
        @Override
        public int compare(CommandInfo o1, CommandInfo o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * 命令排序比较器
     *
     * @since 2016年7月23日 下午4:17:18
     * @author 喵♂呜
     */
    static class CommandComparator implements Comparator<CommandInfo> {
        @Override
        public int compare(CommandInfo o1, CommandInfo o2) {
            if (o1.getSort() > o2.getSort()) {
                return 1;
            } else if (o1.getSort() == o2.getSort()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    static class DefaultHelpGenerator implements HelpGenerator {
        /**
         * 消息配置
         */
        private static String helpTitle = String.format("§6========= %s§6帮助 §aBy §b喵♂呜 §6=========", Log.getPrefix());
        private static String helpBody = "§6/%1$s §a%2$s §e%3$s §6- §b%4$s";
        private static String helpFooter = "§6查看更多的帮助页面 §b请输入 /%s help §e1-%s";
        private static String pageNotFound = "§c不存在的帮助页面 §b请输入 /%s help §e1-%s";
        /**
         * 帮助解析
         */
        private HelpParse helpParse;

        @Override
        public String title() {
            return helpTitle;
        }

        @Override
        public String body(String label, CommandInfo ci) {
            String aliases = Arrays.toString(ci.getCommand().aliases());
            String cmd = ci.getName() + (aliases.length() == 2 ? "" : "§7" + aliases);
            Help help = ci.getHelp();
            return String.format(helpBody, label, cmd, help.possibleArguments(), parse(help.value()));
        }

        @Override
        public String foot(String label, int HELPPAGECOUNT) {
            return String.format(helpFooter, label, HELPPAGECOUNT);
        }

        @Override
        public String notFound(String label, int HELPPAGECOUNT) {
            return String.format(pageNotFound, label, HELPPAGECOUNT);
        }

        /**
         * 解析帮助
         *
         * @param value
         *            参数
         * @return 解析后的帮助
         */
        public String parse(String value) {
            if (helpParse != null) { return helpParse.parse(value); }
            return value;
        }

        /**
         * 设置解析器
         *
         * @param helpParse
         *            帮助解析器
         */
        public void setHelpParse(HelpParse helpParse) {
            this.helpParse = helpParse;
        }
    }
}
