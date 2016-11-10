package pw.yumc.YumCore.plugin.playerpoint;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import pw.yumc.YumCore.kit.PKit;

@SuppressWarnings("deprecation")
public class PPAPI {
    private static PlayerPointsAPI api;
    static {
        Plugin pp = Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints");
        if (pp == null) {
            PKit.disable("未找到 PlayerPoint 插件 停止加载...");
        } else {
            api = ((PlayerPoints) pp).getAPI();
        }
    }

    /**
     * 添加点券
     * 
     * @param player
     *            玩家
     * @param amount
     *            数量
     * @return 是否成功
     */
    public boolean add(String player, int amount) {
        return api.give(player, amount);
    }

    /**
     * 玩家是否指定的点券
     * 
     * @param player
     *            玩家
     * @param amount
     *            数量
     * @return 是否有
     */
    public boolean has(String player, int amount) {
        return api.look(player) >= amount;
    }

    /**
     * 扣除点券
     * 
     * @param player
     *            玩家
     * @param amount
     *            数量
     * @return 是否成功
     */
    public boolean remove(String player, int amount) {
        return api.take(player, amount);
    }

    /**
     * 添加点券
     *
     * @param player
     *            玩家
     * @param amount
     *            数量
     * @return 是否成功
     */
    public boolean add(OfflinePlayer player, int amount) {
        return api.give(player.getUniqueId(), amount);
    }

    /**
     * 玩家是否指定的点券
     *
     * @param player
     *            玩家
     * @param amount
     *            数量
     * @return 是否有
     */
    public boolean has(OfflinePlayer player, int amount) {
        return api.look(player.getUniqueId()) >= amount;
    }

    /**
     * 扣除点券
     *
     * @param player
     *            玩家
     * @param amount
     *            数量
     * @return 是否成功
     */
    public boolean remove(OfflinePlayer player, int amount) {
        return api.take(player.getUniqueId(), amount);
    }
}
