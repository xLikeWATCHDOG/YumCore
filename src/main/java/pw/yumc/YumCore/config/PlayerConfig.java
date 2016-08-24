package pw.yumc.YumCore.config;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 玩家配置管理类
 *
 * @author 喵♂呜
 * @version 1.0
 */
public class PlayerConfig extends FileConfig {
    private static String CONFIG_FOLDER = "userdata";

    /**
     * 获得玩家配置(保存在 PLUGINHELPER 文件夹)
     *
     * @param playername
     *            玩家名称
     */
    public PlayerConfig(final Player playername) {
        super(playername.getName());
    }

    /**
     * 获得玩家配置(保存在 CONFIG_FOLDER 文件夹)
     *
     * @param plugin
     *            插件
     * @param playername
     *            玩家名称
     */
    public PlayerConfig(final Plugin plugin, final Player playername) {
        super(plugin, new File(plugin.getDataFolder(), CONFIG_FOLDER + File.separatorChar + playername.getName()));
    }

    /**
     * 获得玩家配置(保存在 CONFIG_FOLDER 文件夹)
     *
     * @param plugin
     *            插件
     * @param player
     *            玩家
     */
    public PlayerConfig(final Plugin plugin, final String player) {
        super(plugin, new File(plugin.getDataFolder(), CONFIG_FOLDER + File.separatorChar + player));
    }

    /**
     * 获得玩家配置(保存在 PLUGINHELPER 文件夹)
     *
     * @param player
     *            玩家
     */
    public PlayerConfig(final String player) {
        super(player);
    }

}
