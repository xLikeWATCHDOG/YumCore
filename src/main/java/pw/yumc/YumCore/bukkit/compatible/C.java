package pw.yumc.YumCore.bukkit.compatible;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

import com.google.common.base.Charsets;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;

/**
 * Bukkit兼容类
 *
 * @since 2016年7月23日 下午1:04:56
 * @author 喵♂呜
 */
public class C {
    private static Class<?> nmsChatSerializer;
    private static Class<?> nmsIChatBaseComponent;
    private static Class<?> packetType;

    private static Method chatSerializer;
    private static Method getHandle;

    private static String version;
    private static boolean newversion;

    private static Field playerConnection;
    private static Method sendPacket;

    public static boolean init;
    static {
        try {
            version = getNMSVersion();
            newversion = Integer.parseInt(version.split("_")[1]) > 7;
            nmsChatSerializer = Class.forName(a(newversion ? "IChatBaseComponent$ChatSerializer" : "ChatSerializer"));
            chatSerializer = nmsChatSerializer.getMethod("a", String.class);
            nmsIChatBaseComponent = Class.forName(a("IChatBaseComponent"));
            packetType = Class.forName(a("PacketPlayOutChat"));
            Class<?> typeCraftPlayer = Class.forName(b("entity.CraftPlayer"));
            Class<?> typeNMSPlayer = Class.forName(a("EntityPlayer"));
            Class<?> typePlayerConnection = Class.forName(a("PlayerConnection"));
            getHandle = typeCraftPlayer.getMethod("getHandle");
            playerConnection = typeNMSPlayer.getField("playerConnection");
            sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName(a("Packet")));
            init = true;
        } catch (Exception e) {
            Log.w("C 兼容性工具初始化失败 可能造成部分功能不可用!");
            Log.d(e);
        }
    }

    private C() {
    }

    public static String a(String str) {
        return "net.minecraft.server." + version + "." + str;
    }

    public static String b(String str) {
        return "org.bukkit.craftbukkit." + version + "." + str;
    }

    /**
     * 获得NMS版本号
     *
     * @return NMS版本号
     */
    public static String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    /**
     * 给玩家发送Json消息
     *
     * @param receivingPacket
     *            接受信息的玩家
     * @param json
     *            Json信息
     * @param type
     *            类型
     *            0. 消息
     *            2. ActionBar
     */
    public static void sendJson(org.bukkit.entity.Player receivingPacket, String json, int type) {
        Object packet;
        try {
            Object serialized = nmsChatSerializer.getMethod("a", String.class).invoke(null, json);
            if (!version.contains("1_7")) {
                packet = packetType.getConstructor(nmsIChatBaseComponent, byte.class).newInstance(serialized, (byte) type);
            } else {
                packet = packetType.getConstructor(nmsIChatBaseComponent, int.class).newInstance(serialized, type);
            }
            Object player = getHandle.invoke(receivingPacket);
            Object connection = playerConnection.get(player);
            sendPacket.invoke(connection, packet);
        } catch (Exception ex) {
            Log.d("Json发包错误 " + version, ex);
        }
    }

    public static class ActionBar {
        private ActionBar() {
        }

        /**
         * 公告发送ActionBar
         *
         * @param message
         *            需要发送的消息
         */
        public static void broadcast(String message) {
            for (org.bukkit.entity.Player player : C.Player.getOnlinePlayers()) {
                send(player, message);
            }
        }

        /**
         * 公告发送ActionBar
         *
         * @param message
         *            需要发送的消息
         * @param times
         *            需要显示的时间
         */
        public static void broadcast(final String message, final int times) {
            new BukkitRunnable() {
                int time = times;

                @Override
                public void run() {
                    C.Player.getOnlinePlayers().forEach(player -> send(player, message));
                    time--;
                    if (time <= 0) {
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(P.instance, 0, 20);
        }

        /**
         * 公告发送ActionBar(分世界)
         *
         * @param world
         *            需要发送的世界
         * @param message
         *            需要发送的消息
         * @param times
         *            需要显示的时间
         */
        public static void broadcast(final World world, final String message, final int times) {
            new BukkitRunnable() {
                int time = times;

                @Override
                public void run() {
                    C.Player.getOnlinePlayers().stream().filter(player -> player.getWorld().getName().equalsIgnoreCase(world.getName())).forEach(player -> send(player, message));
                    time--;
                    if (time <= 0) {
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(P.instance, 0, 20);
        }

        /**
         * 给玩家发送ActionBar消息
         *
         * @param receivingPacket
         *            接受信息的玩家
         * @param msg
         *            ActionBar信息
         */
        public static void send(org.bukkit.entity.Player receivingPacket, String msg) {
            sendJson(receivingPacket, "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', JSONObject.escape(msg)) + "\"}", 2);
        }

        /**
         * 给玩家发送ActionBar消息
         *
         * @param receivingPacket
         *            接受信息的玩家
         * @param msg
         *            需要发送的消息
         * @param times
         *            需要显示的时间
         */
        public static void send(final org.bukkit.entity.Player receivingPacket, final String msg, final int times) {
            new BukkitRunnable() {
                int time = times;

                @Override
                public void run() {
                    send(receivingPacket, msg);
                    time--;
                    if (time <= 0) {
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(P.instance, 0, 20);
        }
    }

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
                if (getOnlinePlayers.getReturnType() != org.bukkit.entity.Player[].class) {
                    for (Method method : Bukkit.class.getDeclaredMethods()) {
                        if (method.getReturnType() == org.bukkit.entity.Player[].class && method.getName().endsWith("getOnlinePlayers")) {
                            getOnlinePlayers = method;
                        }
                    }
                }
                // getOnlinePlayers end
            } catch (Exception e) {
                Log.w("Player 兼容性工具初始化失败 可能造成部分功能不可用!");
            }
            try {
                // getOfflinePlayer start
                try {
                    gameProfileClass = Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
                } catch (Exception e) {
                    try {
                        gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                    } catch (Exception ignored) {
                    }
                }
                gameProfileConstructor = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
                gameProfileConstructor.setAccessible(true);
                Class<? extends Server> craftServer = Bukkit.getServer().getClass();
                Class<?> craftOfflinePlayer = Class.forName(craftServer.getName().replace("CraftServer", "CraftOfflinePlayer"));
                craftOfflinePlayerConstructor = craftOfflinePlayer.getDeclaredConstructor(craftServer, gameProfileClass);
                craftOfflinePlayerConstructor.setAccessible(true);
                // getOfflinePlayer end
            } catch (Exception e) {
                Log.d(e);
            }
        }

        private Player() {
        }

        /**
         * 获取离线玩家(跳过网络获取)
         *
         * @param playerName
         *            玩家名称
         * @return {@link OfflinePlayer}
         */
        public static OfflinePlayer getOfflinePlayer(String playerName) {
            try {
                Object gameProfile = gameProfileConstructor.newInstance(UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(Charsets.UTF_8)), playerName);
                Object offlinePlayer = craftOfflinePlayerConstructor.newInstance(Bukkit.getServer(), gameProfile);
                return (OfflinePlayer) offlinePlayer;
            } catch (Exception e) {
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
            } catch (Exception e) {
                return Bukkit.getOnlinePlayers();
            }
        }
    }

    public static class Title {
        private static Class<?> packetActions;
        private static Class<?> packetTitle;
        private static Constructor<?> packetTitleSendConstructor;
        private static Constructor<?> packetTitleSetTimeConstructor;
        static {
            try {
                packetActions = Class.forName(a(newversion ? "PacketPlayOutTitle$EnumTitleAction" : "EnumTitleAction"));
                packetTitle = Class.forName(a("PacketPlayOutTitle"));
                packetTitleSendConstructor = packetTitle.getConstructor(packetActions, nmsIChatBaseComponent);
                packetTitleSetTimeConstructor = packetTitle.getConstructor(packetActions, nmsIChatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (Exception ignore) {
                Log.w("Title 兼容性工具初始化失败 可能造成部分功能不可用!");
            }
        }

        private Title() {
        }

        /**
         * 发送Title公告
         *
         * @param title
         *            标题
         * @param subtitle
         *            子标题
         */
        public static void broadcast(String title, String subtitle) {
            for (org.bukkit.entity.Player player : Player.getOnlinePlayers()) {
                send(player, title, subtitle);
            }
        }

        /**
         * 发送Title公告
         *
         * @param title
         *            标题
         * @param subtitle
         *            子标题
         * @param fadeInTime
         *            淡入时间
         * @param stayTime
         *            持续时间
         * @param fadeOutTime
         *            淡出时间
         */
        public static void broadcast(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
            for (org.bukkit.entity.Player player : Player.getOnlinePlayers()) {
                send(player, title, subtitle, fadeInTime, stayTime, fadeOutTime);
            }
        }

        /**
         * 发送Title公告
         *
         * @param world
         *            世界
         * @param title
         *            标题
         * @param subtitle
         *            子标题
         */
        public static void broadcast(World world, String title, String subtitle) {
            C.Player.getOnlinePlayers().stream().filter(player -> player.getWorld().getName().equalsIgnoreCase(world.getName())).forEach(player -> send(player, title, subtitle));
        }

        /**
         * 重置玩家的Title
         *
         * @param recoverPlayer
         *            接受的玩家
         * @throws Exception
         *             异常
         */
        public static void reset(org.bukkit.entity.Player recoverPlayer) throws Exception {
            // Send timings first
            Object player = getHandle.invoke(recoverPlayer);
            Object connection = playerConnection.get(player);
            Object[] actions = packetActions.getEnumConstants();
            Object packet = packetTitleSendConstructor.newInstance(actions[4], null);
            sendPacket.invoke(connection, packet);
        }

        /**
         * 发送Titile(默认时间 1 2 1)
         *
         * @param receivingPacket
         *            接受信息的玩家
         * @param title
         *            标题
         * @param subtitle
         *            子标题
         */
        public static void send(org.bukkit.entity.Player receivingPacket, String title, String subtitle) {
            send(receivingPacket, title, subtitle, 1, 2, 1);
        }

        /**
         * 发送Titile
         *
         * @param receivingPacket
         *            接受信息的玩家
         * @param title
         *            标题
         * @param subtitle
         *            子标题
         * @param fadeInTime
         *            淡入时间
         * @param stayTime
         *            持续时间
         * @param fadeOutTime
         *            淡出时间
         */
        public static void send(org.bukkit.entity.Player receivingPacket, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
            if (packetTitle != null) {
                try {
                    // First reset previous settings
                    reset(receivingPacket);
                    // Send timings first
                    Object player = getHandle.invoke(receivingPacket);
                    Object connection = playerConnection.get(player);
                    Object[] actions = packetActions.getEnumConstants();
                    Object packet;
                    // Send if set
                    if ((fadeInTime != -1) && (fadeOutTime != -1) && (stayTime != -1)) {
                        packet = packetTitleSetTimeConstructor.newInstance(actions[2], null, fadeInTime * 20, stayTime * 20, fadeOutTime * 20);
                        sendPacket.invoke(connection, packet);
                    }
                    // Send title
                    Object serialized = chatSerializer.invoke(null, "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', title) + "\"}");
                    packet = packetTitleSendConstructor.newInstance(actions[0], serialized);
                    sendPacket.invoke(connection, packet);
                    if (!"".equals(subtitle)) {
                        // Send subtitle if present
                        serialized = chatSerializer.invoke(null, "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', subtitle) + "\"}");
                        packet = packetTitleSendConstructor.newInstance(actions[1], serialized);
                        sendPacket.invoke(connection, packet);
                    }
                } catch (Exception e) {
                    Log.d(e);
                }
            }
        }
    }
}
