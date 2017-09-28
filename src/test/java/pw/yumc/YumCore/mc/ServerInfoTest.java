package pw.yumc.YumCore.mc;


import org.junit.Test;

import lombok.val;

/**
 * Created with IntelliJ IDEA
 *
 * @author 喵♂呜
 * Created on 2017/9/28 18:50.
 */
public class ServerInfoTest {
    @Test
    public void test() {
        val info = new ServerInfo("play.i5mc.com");
        info.fetchData();
    }
}