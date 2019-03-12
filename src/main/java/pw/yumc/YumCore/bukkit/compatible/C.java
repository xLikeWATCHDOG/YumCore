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

import pw.yumc.YumCore.annotation.NotProguard;
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
    private static Class<?> nmsChatMessageTypeClass;

    private static Constructor<?> packetTypeConstructor;

    private static Method chatSerializer;
    private static Method getHandle;
    private static Method nmsChatMessageTypeClassValueOf;

    private static String version;
    private static boolean newversion;

    private static Field playerConnection;
    private static Method sendPacket;

    private static Object[] chatMessageTypes;

    public static boolean init;
    public static boolean cauldron1710;
    public static boolean uranium;
    public static boolean titlePAB;
    static {
        version=getNMSVersion();
        titlePAB=false;

        try {
            cauldron1710 =version.equals("v1_7_R4")&&Package.getPackage("net.minecraftforge.cauldron1710")!=null;
            if(cauldron1710) {
                try{
                    Class.forName("cc.uraniummc.Uranium");
                    uranium=true;
                }catch (Exception e){
                    Log.d("检测到不是Uranium服务端");
                }
                newversion=false;
                nmsChatSerializer = Class.forName("net.minecraft.util.IChatComponent$Serializer");
                chatSerializer = nmsChatSerializer.getMethod("func_150699_a", String.class);
                nmsIChatBaseComponent = Class.forName("net.minecraft.util.IChatComponent");
                packetType = Class.forName("net.minecraft.network.play.server.S02PacketChat");
                Arrays.stream(packetType.getConstructors()).forEach(c -> {
                    if (c.getParameterTypes().length == 2&&uranium) {
                        packetTypeConstructor = c;
                    }else if(c.getParameterTypes().length==1){
                        packetTypeConstructor = c;
                    }
                });
            }else {
                newversion = Integer.parseInt(version.split("_")[1]) > 7;
                titlePAB=newversion;
                nmsChatSerializer = Class.forName(a(newversion ? "IChatBaseComponent$ChatSerializer" : "ChatSerializer"));
                chatSerializer = nmsChatSerializer.getMethod("a", String.class);
                nmsIChatBaseComponent = Class.forName(a("IChatBaseComponent"));
                packetType = Class.forName(a("PacketPlayOutChat"));
                Arrays.stream(packetType.getConstructors()).forEach(c -> {
                    if (c.getParameterTypes().length == 2) {
                        packetTypeConstructor = c;
                    }
                });
            }
            if(!cauldron1710||uranium) {
                try {
                    nmsChatMessageTypeClass = packetTypeConstructor.getParameterTypes()[1];
                    if (nmsChatMessageTypeClass.isEnum()) {
                        chatMessageTypes = nmsChatMessageTypeClass.getEnumConstants();
                    } else {
                        switch (nmsChatMessageTypeClass.getName()) {
                            case "int":
                                nmsChatMessageTypeClass = Integer.class;
                            case "byte":
                                nmsChatMessageTypeClass = Byte.class;
                        }
                        nmsChatMessageTypeClassValueOf = nmsChatMessageTypeClass.getDeclaredMethod("valueOf", String.class);
                    }
                } catch (Exception e) {
                    packetTypeConstructor = packetType.getConstructor(String.class);
                }
            }
            Class<?> typeCraftPlayer = Class.forName(b("entity.CraftPlayer"));
            getHandle = typeCraftPlayer.getMethod("getHandle");
            if(cauldron1710) {
                Class<?> typeNMSPlayer = Class.forName("net.minecraft.entity.player.EntityPlayerMP");
                Class<?> typePlayerConnection = Class.forName("net.minecraft.network.NetHandlerPlayServer");
                playerConnection = typeNMSPlayer.getField("field_71135_a");
                sendPacket = typePlayerConnection.getMethod("func_147359_a", Class.forName("net.minecraft.network.Packet"));
            }else{
                Class<?> typeNMSPlayer = Class.forName(a("EntityPlayer"));
                Class<?> typePlayerConnection = Class.forName(a("PlayerConnection"));
                playerConnection = typeNMSPlayer.getField("playerConnection");
                sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName(a("Packet")));
            }
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
        try {
            Object serialized = chatSerializer.invoke(null, json);
            Object player = getHandle.invoke(receivingPacket);
            Object connection = playerConnection.get(player);
            Object typeObj;
            if(titlePAB){
                sendPacket.invoke(connection,Title.packetTitleSendConstructor.newInstance(Title.actions[2],serialized));
                return;
            }else {
                if (nmsChatMessageTypeClass==null) {
                    sendPacket.invoke(connection, packetTypeConstructor.newInstance(serialized));
                    return;
                } else {
                    typeObj = chatMessageTypes == null ? nmsChatMessageTypeClassValueOf.invoke(null, String.valueOf(type)) : chatMessageTypes[type];
                }
            }
            sendPacket.invoke(connection, packetTypeConstructor.newInstance(serialized,typeObj));
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
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
        private static Object[] actions;
        static {
            if(cauldron1710 &&uranium) {
                try {
                    packetActions = Class.forName("cc.uraniummc.packet.S45PacketTitle$Type");
                    packetTitle = Class.forName("cc.uraniummc.packet.S45PacketTitle");
                    packetTitleSendConstructor = packetTitle.getConstructor(packetActions, nmsIChatBaseComponent);
                    packetTitleSetTimeConstructor = packetTitle.getConstructor(packetActions, nmsIChatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                    actions = packetActions.getEnumConstants();
                } catch (Exception ignore) {
                    titlePAB=false;
                    Log.w("Title(Uranium 1.7.10) 兼容性工具初始化失败 可能造成部分功能不可用!");
                    Log.d(ignore);
                }
            }else {
                try {
                    packetActions = Class.forName(a(newversion ? "PacketPlayOutTitle$EnumTitleAction" : "EnumTitleAction"));
                    packetTitle = Class.forName(a("PacketPlayOutTitle"));
                    packetTitleSendConstructor = packetTitle.getConstructor(packetActions, nmsIChatBaseComponent);
                    packetTitleSetTimeConstructor = packetTitle.getConstructor(packetActions, nmsIChatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                    actions = packetActions.getEnumConstants();
                    for(Object data:actions){
                        Enum enum_=(Enum)data;
                        if(enum_.name().equals("ACTIONBAR")){
                            titlePAB=true;
                        }
                    }
                } catch (Exception ignore) {
                    Log.w("Title 兼容性工具初始化失败 可能造成部分功能不可用!");
                    Log.d(ignore);
                }
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
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
        @NotProguard
        public static void reset(org.bukkit.entity.Player recoverPlayer) throws Exception {
            // Send timings first
            Object player = getHandle.invoke(recoverPlayer);
            Object connection = playerConnection.get(player);
            Object packet = packetTitleSendConstructor.newInstance(titlePAB?actions[5]:actions[4], null);
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
        @NotProguard
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
        @NotProguard
        public static void send(org.bukkit.entity.Player receivingPacket, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
            if (packetTitle != null) {
                try {
                    // First reset previous settings
                    reset(receivingPacket);
                    // Send timings first
                    Object player = getHandle.invoke(receivingPacket);
                    Object connection = playerConnection.get(player);
                    Object packet;
                    // Send if set
                    if ((fadeInTime != -1) && (fadeOutTime != -1) && (stayTime != -1)) {
                        packet = packetTitleSetTimeConstructor.newInstance(titlePAB?actions[3]:actions[2], null, fadeInTime * 20, stayTime * 20, fadeOutTime * 20);
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
