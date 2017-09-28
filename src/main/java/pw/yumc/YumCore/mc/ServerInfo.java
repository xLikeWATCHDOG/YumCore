package pw.yumc.YumCore.mc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Pattern;

import lombok.Data;
import pw.yumc.YumCore.annotation.NotProguard;

/**
 * Minecraft服务器数据获取类
 *
 * @author 喵♂呜
 * @since 2017/1/26 0026
 */
@Data
@NotProguard
public class ServerInfo {
    private static byte PACKET_HANDSHAKE = 0x00, PACKET_STATUSREQUEST = 0x00, PACKET_PING = 0x01;
    private static int PROTOCOL_VERSION = 4;
    private static int STATUS_HANDSHAKE = 1;
    private static Pattern pattern = Pattern.compile(".*\"description\":\"(.*)\".*");
    /**
     * {
     * "version": {"name": "BungeeCord 1.8.x, 1.9.x, 1.10.x, 1.11.x", "protocol": 316},
     * "players": {"max": 922, "online": 921},
     * "description": {
     * "extra": [
     * {"color": "white", "text": "                       "},
     * {"color": "aqua", "bold": true, "text": "梦世界 "},
     * {"color": "red","bold": true, "obfuscated": true, "text": "|"},
     * {"color": "white", "text": " "},
     * {"color": "white", "bold": true, "text": "i5mc.com\n"},
     * {"color": "white", "text": "       " },
     * {"color": "yellow", "bold": true, "text": "★"},
     * {"color": "red", "bold": true, "text": "1.8-1.11"},
     * {"color": "yellow", "bold": true, "text": "★  ★"},
     * {"color": "yellow", "text": "黄金周末"},
     * {"color": "gray", "text": "-"},
     * {"color": "green", "text": "空岛战争"},
     * {"color": "red", "bold": true, "text": "双倍硬币"},
     * {"color": "green", "text": "奖励"},
     * {"color": "yellow", "bold": true, "text": "★"}
     * ],"text": "" }
     * }
     */
    private String address = "localhost";
    private int port = 25565;
    private int timeout = 1500;
    private int pingVersion = -1;
    private int protocolVersion = -1;
    private String gameVersion = "初始化中...";
    private String motd = "初始化中...";
    private int playersOnline = -1;
    private int maxPlayers = -1;

    /**
     * Minecraft服务器数据获取类
     *
     * @param address
     *         服务器地址 默认端口25565
     */
    public ServerInfo(String address) {
        this(address, 25565);
    }

    /**
     * Minecraft服务器数据获取类
     *
     * @param address
     *         服务器地址
     * @param port
     *         服务器端口
     */
    public ServerInfo(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * 获取服务器数据
     *
     * @return 是否获取成功
     */
    public boolean fetchData() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(getAddress(), getPort()), getTimeout());

            final DataInputStream in = new DataInputStream(socket.getInputStream());
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //> Handshake
            ByteArrayOutputStream handshake_bytes = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(handshake_bytes);

            handshake.writeByte(PACKET_HANDSHAKE);
            writeVarInt(handshake, PROTOCOL_VERSION);
            writeVarInt(handshake, getAddress().length());
            handshake.writeBytes(getAddress());
            handshake.writeShort(getPort());
            writeVarInt(handshake, STATUS_HANDSHAKE);

            writeVarInt(out, handshake_bytes.size());
            out.write(handshake_bytes.toByteArray());

            //> Status request

            out.writeByte(0x01); // Size of packet
            out.writeByte(PACKET_STATUSREQUEST);

            //< Status response

            readVarInt(in); // Size
            int id = readVarInt(in);

            int length = readVarInt(in);

            byte[] data = new byte[length];
            in.readFully(data);
            String json = new String(data, "UTF-8");
            System.out.println(json);
            //            //> Ping
            //
            //            out.writeByte(0x09); // Size of packet
            //            out.writeByte(PACKET_PING);
            //            out.writeLong(System.currentTimeMillis());
            //
            //            //< Ping
            //            readVarInt(in); // Size
            //            id = readVarInt(in);

            // Close
            handshake.close();
            handshake_bytes.close();
            out.close();
            in.close();
        } catch (IOException exception) {
            gameVersion = "获取失败!";
            motd = "获取失败!";
            return false;
        }
        return true;
    }

    /**
     * @author thinkofdeath
     * See: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
     */
    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5)
                throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128)
                break;
        }
        return i;
    }

    /**
     * @author thinkofdeath
     * See: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
     */
    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }
}
