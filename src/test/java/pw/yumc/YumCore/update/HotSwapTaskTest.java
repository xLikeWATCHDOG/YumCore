package pw.yumc.YumCore.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.ScriptException;

import org.junit.Test;

import pw.yumc.YumCore.engine.MiaoScriptEngine;

/**
 * Created with IntelliJ IDEA
 *
 * @author 喵♂呜
 * Created on 2017/8/29 20:24.
 */
public class HotSwapTaskTest {
    private MiaoScriptEngine engine;

    public HotSwapTaskTest() {
        this.engine = new MiaoScriptEngine();
        engine.put("$", this);
    }

    @Test
    public void test() throws FileNotFoundException, ScriptException {
        engine.eval(new FileReader(new File("src/test/resources/hotswap.js")));
    }

    @Test
    public void testClient() throws FileNotFoundException, ScriptException {
        engine.eval(new FileReader(new File("src/test/resources/nio-client.js")));
    }
}