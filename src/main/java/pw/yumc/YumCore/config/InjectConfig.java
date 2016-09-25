package pw.yumc.YumCore.config;

import java.io.File;

/**
 * 配置自动载入类
 *
 * @since 2016年7月5日 上午8:53:57
 * @author 喵♂呜
 */
public abstract class InjectConfig extends AbstractInjectConfig {
    protected FileConfig config;

    public InjectConfig() {
        this(new FileConfig());
    }

    public InjectConfig(final File file) {
        this(new FileConfig(file));
    }

    public InjectConfig(final FileConfig config) {
        this.config = config;
        inject(config);
    }

    public InjectConfig(final String name) {
        this(new FileConfig(name));
    }

    /**
     * 获得配置文件
     *
     * @return 配置文件
     */
    public FileConfig getConfig() {
        return config;
    }

    /**
     * 重载配置文件
     */
    public void reload() {
        config.reload();
        inject(config);
    }

    /**
     * 自动化保存
     */
    public void save() {
        save(config);
        config.save();
    }
}
