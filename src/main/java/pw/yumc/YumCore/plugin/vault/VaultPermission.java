package pw.yumc.YumCore.plugin.vault;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.kit.PKit;

/**
 * Vault 权限管理
 *
 * @since 2016年5月12日 下午5:02:03
 * @author 喵♂呜
 */
public class VaultPermission extends VaultBase {
    private static Permission permission;

    static {
        final RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        if (permission == null) {
            PKit.disable("已加载 Vault 但是未找到权限相关插件 停止加载...");
        } else {
            Log.info("发现 Vault 使用权限管理系统 " + permission.getName());
        }
    }

    /**
     * 给玩家添加权限
     *
     * @param player
     *            玩家
     * @param perm
     *            权限
     * @return 结果
     */
    public static boolean add(final Player player, final String perm) {
        return permission.playerAdd(player, perm);
    }

    /**
     * 获得玩家权限组
     *
     * @param player
     *            玩家
     * @return 权限组
     */
    public static String getGroup(final Player player) {
        return permission.getPrimaryGroup(player);
    }

    /**
     * 获得Permission实例
     *
     * @return {@link Permission}
     */
    public static Permission getPermission() {
        return permission;
    }

    /**
     * 判断玩家是否有权限
     *
     * @param player
     *            玩家
     * @param perm
     *            权限
     * @return 结果
     */
    public static boolean has(final Player player, final String perm) {
        return permission.has(player, perm);
    }

    /**
     * 移除玩家权限
     *
     * @param player
     *            玩家
     * @param perm
     *            权限
     * @return 结果
     */
    public static boolean remove(final Player player, final String perm) {
        return permission.playerRemove(player, perm);
    }
}
