package pw.yumc.YumCore.update;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.bukkit.plugin.Plugin;
import org.junit.Test;
import org.xml.sax.SAXException;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.plugin.FakePlugin;

/**
 * @author 喵♂呜
 * @since 2017/6/1
 */
public class SubscribeTaskTest {
    private Plugin plugin = new FakePlugin("YumCore", "1.0");

    @Test
    public void test() throws IOException, SAXException, ParserConfigurationException {
        SubscribeTask.VersionInfo info = new SubscribeTask.VersionInfo(plugin, "master", true);
        info.update();
        Log.d("Currect Version: %s", info.getVersion());
        Log.d("New Version: %s", info.getNewVersion());
        boolean nu = info.needUpdate(info.getNewVersion(), plugin.getDescription().getVersion());
        Log.d("Need Update: %s", nu);
        Log.d("File URL: %s", SubscribeTask.UpdateType.WS.getDownloadUrl(plugin, info.getNewVersion()));
    }
}