package pw.yumc.YumCore.bukkit.compatible;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import com.google.common.base.Charsets;

import pw.yumc.YumCore.bukkit.Log;

/**
 * Bukkit兼容类
 *
 * @since 2016年7月23日 下午1:04:56
 * @author 喵♂呜
 */
public class C {
    /**
     * Bukkit Player兼容类
     * 
     * @since 2016年7月23日 下午4:33:40
     * @author 喵♂呜
     */
    public static class Player {
        private static Class<?> gameProfileClass;
        private static Constructor<?> gameProfileConstructor;
        private static Constructor<?> craftOfflinePlayerConstructor;

        private static Method getOnlinePlayers;

        static {
            try {
                // getOnlinePlayers start
                getOnlinePlayers = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
                if (getOnlinePlayers.getReturnType() != Player[].class) {
                    getOnlinePlayers = Bukkit.class.getDeclaredMethod("_INVALID_getOnlinePlayers");
                }
                // getOnlinePlayers end
                // getOfflinePlayer start
                try {
                    gameProfileClass = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
                } catch (final ClassNotFoundException e) {
                    try {
                        gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                    } catch (final ClassNotFoundException e1) {
                    }
                }
                gameProfileConstructor = gameProfileClass.getDeclaredConstructor(new Class[] { UUID.class, String.class });
                gameProfileConstructor.setAccessible(true);
                final Class<? extends Server> craftServer = Bukkit.getServer().getClass();
                final Class<?> craftOfflinePlayer = Class.forName(craftServer.getName().replace("CraftServer", "CraftOfflinePlayer"));
                craftOfflinePlayerConstructor = craftOfflinePlayer.getDeclaredConstructor(new Class[] { craftServer, gameProfileClass });
                craftOfflinePlayerConstructor.setAccessible(true);
                // getOfflinePlayer end
            } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                Log.warning(Player.class.getSimpleName() + "兼容性工具初始化失败 可能造成部分功能不可用!");
            }
        }

        /**
         * 获取离线玩家(跳过网络获取)
         *
         * @param playerName
         *            玩家名称
         * @return {@link OfflinePlayer}
         */
        public static OfflinePlayer getOfflinePlayer(final String playerName) {
            try {
                final Object gameProfile = gameProfileConstructor.newInstance(new Object[] { UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(Charsets.UTF_8)), playerName });
                final Object offlinePlayer = craftOfflinePlayerConstructor.newInstance(new Object[] { Bukkit.getServer(), gameProfile });
                return (OfflinePlayer) offlinePlayer;
            } catch (final Throwable var5) {
                return Bukkit.getOfflinePlayer(playerName);
            }
        }

        /**
         * 获取在线玩家
         *
         * @return 在线玩家
         */
        public static Collection<? extends org.bukkit.entity.Player> getOnlinePlayers() {
            try {
                return Arrays.asList((org.bukkit.entity.Player[]) getOnlinePlayers.invoke(null));
            } catch (final Throwable e) {
                return Bukkit.getOnlinePlayers();
            }
        }
    }
}
