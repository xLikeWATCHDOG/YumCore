package pw.yumc.YumCore.bukkit.compatible;

import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Chat {
    private static final String version;
    private static final BukkitChatInvoke bukkitChatInvoke;

    static {
        version = getNMSVersion();
        int subVersion = Integer.parseInt(version.split("_")[1]);
        if (subVersion >= 19) {
            bukkitChatInvoke = new BukkitChatInvoke_1_19();
        } else if (subVersion >= 17) {
            bukkitChatInvoke = new BukkitChatInvoke_1_17_1();
        } else if (subVersion >= 16) {
            bukkitChatInvoke = new BukkitChatInvoke_1_16_5();
        } else if (subVersion >= 8) {
            bukkitChatInvoke = new BukkitChatInvoke_1_8();
        } else {
            bukkitChatInvoke = new BukkitChatInvoke_1_7_10();
        }
        bukkitChatInvoke.init();
    }

    public static BukkitChatInvoke getBukkitChatInvoke() {
        return bukkitChatInvoke;
    }

    /**
     * 获得NMS版本号
     *
     * @return NMS版本号
     */
    public static String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static Class<?> nmsCls(String str) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + str);
    }

    public static Class<?> obcCls(String str) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version + "." + str);
    }

    public static abstract class BukkitChatInvoke {
        protected boolean downgrade = false;

        protected Method chatSerializer;
        protected Constructor<?> packetTypeConstructor;
        protected Method getHandle;
        protected Field playerConnection;
        protected Method sendPacket;
        protected Object[] chatMessageTypes;

        protected Method componentSerializer;

        BukkitChatInvoke() {
            init();
        }

        public void init() {
            try {
                Class<?> nmsChatSerializerClass = this.getNmsChatSerializerClass();
                this.chatSerializer = this.getNmsChatSerializerMethod(nmsChatSerializerClass);
                Constructor<?>[] constructors = this.getPacketPlayOutChatClass().getConstructors();
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterCount() == 2 || constructor.getParameterCount() == 3) {
                        this.packetTypeConstructor = constructor;
                        if (this.isMatchPacketPlayOutChatClassConstructor(constructor)) {
                            break;
                        }
                    }
                }
                this.getHandle = this.getGetHandleMethod();
                this.playerConnection = this.getPlayerConnectionField(this.getHandle.getReturnType());
                this.sendPacket = this.getSendPacketMethod(this.playerConnection.getType(), this.getPacketClass());
            } catch (Throwable ex) {
                this.downgrade = true;
                try {
                    this.componentSerializer = Class.forName("net.md_5.bungee.chat.ComponentSerializer").getMethod("parse", String.class);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        @SneakyThrows
        void json(Player receivingPacket, String json) {
            if (this.downgrade) {
                receivingPacket.spigot().sendMessage((BaseComponent[]) this.componentSerializer.invoke(null, json));
            } else {
                this.send(receivingPacket, json, 0);
            }
        }

        @SneakyThrows
        void send(Player receivingPacket, String json, int type) {
            this.sendPacket(receivingPacket, this.getPacketPlayOutChat(receivingPacket, json, type));
        }

        @SneakyThrows
        void sendPacket(Player receivingPacket, Object packet) {
            this.sendPacket.invoke(playerConnection.get(getHandle.invoke(receivingPacket)), packet);
        }

        abstract boolean isMatchPacketPlayOutChatClassConstructor(Constructor<?> constructor);

        abstract Class<?> getNmsChatSerializerClass();

        abstract Method getNmsChatSerializerMethod(Class<?> nmsChatSerializerClass);

        abstract Class<?> getPacketPlayOutChatClass();

        abstract Field getPlayerConnectionField(Class<?> nmsEntityPlayerClass);

        abstract Method getGetHandleMethod();

        abstract Class<?> getPacketClass();

        abstract Method getSendPacketMethod(Class<?> playerConnectionClass, Class<?> packetClass);

        abstract Object getPacketPlayOutChat(Player player, String json, int type);
    }

    public static class BukkitChatInvokeBase extends BukkitChatInvoke {
        @Override
        protected boolean isMatchPacketPlayOutChatClassConstructor(Constructor<?> constructor) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types[1].isEnum()) {
                this.chatMessageTypes = types[1].getEnumConstants();
                return true;
            }
            return types[1].getName().equals("int");
        }

        @Override
        @SneakyThrows
        Class<?> getNmsChatSerializerClass() {
            return nmsCls("ChatSerializer");
        }

        @Override
        @SneakyThrows
        Method getNmsChatSerializerMethod(Class<?> nmsChatSerializerClass) {
            return nmsChatSerializerClass.getMethod("a", String.class);
        }

        @Override
        @SneakyThrows
        Class<?> getPacketPlayOutChatClass() {
            return nmsCls("PacketPlayOutChat");
        }

        @Override
        @SneakyThrows
        Field getPlayerConnectionField(Class<?> nmsEntityPlayerClass) {
            return nmsEntityPlayerClass.getField("playerConnection");
        }

        @Override
        @SneakyThrows
        Method getGetHandleMethod() {
            return obcCls("entity.CraftPlayer").getMethod("getHandle");
        }

        @Override
        @SneakyThrows
        Class<?> getPacketClass() {
            return nmsCls("Packet");
        }

        @Override
        @SneakyThrows
        Method getSendPacketMethod(Class<?> playerConnectionClass, Class<?> packetClass) {
            return playerConnectionClass.getMethod("sendPacket", packetClass);
        }

        @Override
        @SneakyThrows
        Object getPacketPlayOutChat(Player player, String json, int type) {
            return packetTypeConstructor.newInstance(this.chatSerializer.invoke(null, json), type);
        }
    }

    public static class BukkitChatInvoke_1_7_10 extends BukkitChatInvokeBase {

    }

    public static class BukkitChatInvoke_1_8 extends BukkitChatInvoke_1_7_10 {
        @Override
        @SneakyThrows
        Object getPacketPlayOutChat(Player player, String json, int type) {
            return packetTypeConstructor.newInstance(this.chatSerializer.invoke(null, json), this.chatMessageTypes[type]);
        }

        @Override
        @SneakyThrows
        Class<?> getNmsChatSerializerClass() {
            return nmsCls("IChatBaseComponent$ChatSerializer");
        }
    }

    public static class BukkitChatInvoke_1_16_5 extends BukkitChatInvoke_1_8 {
        @Override
        @SneakyThrows
        Object getPacketPlayOutChat(Player player, String json, int type) {
            return packetTypeConstructor.newInstance(this.chatSerializer.invoke(null, json), this.chatMessageTypes[type], player.getUniqueId());
        }
    }

    public static class BukkitChatInvoke_1_17_1 extends BukkitChatInvoke_1_16_5 {
        @Override
        @SneakyThrows
        Class<?> getPacketPlayOutChatClass() {
            return Class.forName("net.minecraft.network.protocol.game.PacketPlayOutChat");
        }

        @Override
        @SneakyThrows
        Class<?> getNmsChatSerializerClass() {
            return Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
        }

        @Override
        @SneakyThrows
        Field getPlayerConnectionField(Class<?> nmsEntityPlayerClass) {
            return nmsEntityPlayerClass.getField("b");
        }

        @Override
        @SneakyThrows
        Class<?> getPacketClass() {
            return Class.forName("net.minecraft.network.protocol.Packet");
        }
    }

    public static class BukkitChatInvoke_1_18_2 extends BukkitChatInvoke_1_17_1 {
        @Override
        @SneakyThrows
        Method getSendPacketMethod(Class<?> playerConnectionClass, Class<?> packetClass) {
            return playerConnectionClass.getMethod("a", packetClass);
        }
    }

    public static class BukkitChatInvoke_1_19 extends BukkitChatInvoke_1_18_2 {
        @Override
        @SneakyThrows
        protected boolean isMatchPacketPlayOutChatClassConstructor(Constructor<?> constructor) {
            Class<?>[] types = constructor.getParameterTypes();
            return types[0] == Class.forName("net.minecraft.network.chat.IChatBaseComponent") && types[1] == boolean.class;
        }

        @Override
        @SneakyThrows
        Class<?> getPacketPlayOutChatClass() {
            return Class.forName("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
        }

        @Override
        @SneakyThrows
        Object getPacketPlayOutChat(Player player, String json, int type) {
            Object component = this.chatSerializer.invoke(null, json);
            return this.packetTypeConstructor.newInstance(component, type == 1);
        }
    }
}
