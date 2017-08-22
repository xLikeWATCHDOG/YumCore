package pw.yumc.YumCore.bukkit;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA
 *
 * @author 喵♂呜
 * Created on 2017/8/22 15:16.
 */
public class LogTest {
    @Test
    public void testSimpleNames() {
        Assert.assertTrue("[LogTest, LogTest, null]".equals(Log.getSimpleNames(this, this.getClass(), null)));
    }
}