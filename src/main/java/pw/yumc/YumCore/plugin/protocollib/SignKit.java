package pw.yumc.YumCore.plugin.protocollib;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pw.yumc.YumCore.bukkit.P;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 木牌工具类
 *
 * @since 2016年7月7日 上午9:38:28
 * @author 喵♂呜
 */
public class SignKit extends ProtocolLibBase {
    private static boolean newVer = true;
    static {
        try {
            GameMode.SPECTATOR.name();
        } catch (Exception e) {
            newVer = false;
        }
    }

    /**
     * 打开木牌
     *
     * @param player
     *            玩家
     * @param lines
     *            木牌内容
     * @throws InvocationTargetException
     *             调用异常
     */
    public static void open(Player player, String[] lines) throws InvocationTargetException {
        Location loc = player.getLocation();
        int x = loc.getBlockX(), y = 0, z = loc.getBlockZ();
        if (newVer) {
            // Set
            PacketContainer packet = manager.createPacket(Server.BLOCK_CHANGE);
            packet.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
            packet.getBlockData().write(0, WrappedBlockData.createData(Material.SIGN_POST));
            manager.sendServerPacket(player, packet);

            // Update
            packet = manager.createPacket(Server.UPDATE_SIGN);
            packet.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
            packet.getChatComponentArrays().write(0,
                    new WrappedChatComponent[] { WrappedChatComponent.fromText(lines[0]), WrappedChatComponent.fromText(lines[1]), WrappedChatComponent.fromText(lines[2]),
                                                 WrappedChatComponent.fromText(lines[3]) });

            manager.sendServerPacket(player, packet);

            // Edit
            packet = manager.createPacket(Server.OPEN_SIGN_ENTITY);
            packet.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
            manager.sendServerPacket(player, packet);
        } else {
            // Set
            PacketContainer packet = manager.createPacket(Server.BLOCK_CHANGE);
            packet.getIntegers().write(0, x).write(1, y).write(2, z).write(3, 0);
            packet.getBlocks().write(0, Material.SIGN_POST);
            manager.sendServerPacket(player, packet);

            // Update
            packet = manager.createPacket(Server.UPDATE_SIGN);
            packet.getIntegers().write(0, x).write(1, y).write(2, z);
            packet.getStringArrays().write(0, lines);
            manager.sendServerPacket(player, packet);

            // Edit
            packet = manager.createPacket(Server.OPEN_SIGN_ENTITY);
            packet.getIntegers().write(0, x).write(1, y).write(2, z);
            manager.sendServerPacket(player, packet);
        }
    }

    /**
     * 初始化监听器
     */
    public void init() {
        manager.addPacketListener(new SignUpdateListen());
    }

    /**
     * 木牌更新事件
     *
     * @since 2016年7月7日 上午9:59:07
     * @author 喵♂呜
     */
    public static class SignUpdateEvent extends Event implements Cancellable {
        private static HandlerList handlers = new HandlerList();
        private Player player;
        private String[] lines;
        private boolean cancel = false;

        public SignUpdateEvent(Player player, String[] lines) {
            this.player = player;
            this.lines = lines;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }

        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        /**
         * 木牌内容
         *
         * @return lines
         */
        public String[] getLines() {
            return lines;
        }

        /**
         * 触发玩家
         *
         * @return player
         */
        public Player getPlayer() {
            return player;
        }

        /*
         * @see org.bukkit.event.Cancellable#isCancelled()
         */
        @Override
        public boolean isCancelled() {
            return cancel;
        }

        /*
         * @see org.bukkit.event.Cancellable#setCancelled(boolean)
         */
        @Override
        public void setCancelled(boolean cancel) {
            this.cancel = cancel;
        }

    }

    /**
     * 木牌监听类
     *
     * @since 2016年7月7日 上午9:58:59
     * @author 喵♂呜
     */
    public static class SignUpdateListen extends PacketAdapter implements PacketListener {

        public SignUpdateListen() {
            super(P.instance, Client.UPDATE_SIGN);
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
            Player player = event.getPlayer();
            PacketContainer packet = event.getPacket();
            List<String> lines = new ArrayList<>();
            if (newVer) {
                WrappedChatComponent[] input1_8 = packet.getChatComponentArrays().read(0);
                for (WrappedChatComponent wrappedChatComponent : input1_8) {
                    lines.add(subString(wrappedChatComponent.getJson()));
                }
            } else {
                String[] input = packet.getStringArrays().getValues().get(0);
                lines.addAll(Arrays.asList(input));
            }
            SignUpdateEvent sue = new SignUpdateEvent(player, lines.toArray(new String[0]));
            Bukkit.getPluginManager().callEvent(sue);
            event.setCancelled(sue.isCancelled());
        }

        /**
         * 去除首尾
         * 
         * @param string
         *            字符串
         * @return 处理后的字符串
         */
        private String subString(String string) {
            return string.substring(1, string.length() - 1);
        }
    }

}
