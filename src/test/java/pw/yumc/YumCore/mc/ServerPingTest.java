package pw.yumc.YumCore.mc;

import org.junit.Test;

import lombok.val;
import pw.yumc.YumCore.bukkit.Log;

/**
 * Created with IntelliJ IDEA
 *
 * @author 喵♂呜
 * Created on 2017/9/29 13:05.
 */
public class ServerPingTest {
    @Test
    public void test() {
        val ping = new ServerPing("play.i5mc.com");
        ping.fetchData();
        Log.i("version: %s max: %s online: %s motd: %s",
              ping.getVersionName(), ping.getPlayersMax(), ping.getPlayersOnline(), ping.getMotd());
    }
}