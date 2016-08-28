package pw.yumc.YumCore.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

import pw.yumc.YumCore.bukkit.Log;

/**
 * 配置自动载入类
 *
 * @since 2016年7月5日 上午8:53:57
 * @author 喵♂呜
 */
public abstract class InjectConfig extends AbstractInjectConfig {
    protected FileConfig config;

    public InjectConfig() {
        config = new FileConfig();
        inject();
    }

    public InjectConfig(final File file) {
        config = new FileConfig(file);
        inject();
    }

    public InjectConfig(final String name) {
        config = new FileConfig(name);
        inject();
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
        inject();
    }

    /**
     * 设置字段数据
     *
     * @param path
     *            配置路径
     * @param field
     *            字段
     * @throws IllegalArgumentException
     */
    @Override
    protected void setField(final String path, final Field field) throws IllegalArgumentException {
        final String realPath = path.replace("_", ".");
        if (config.isSet(realPath)) {
            final Object value = config.get(realPath);
            try {
                if (!commonParse(config, realPath, field)) {
                    if (config.isString(realPath)) {
                        field.set(this, config.getMessage(realPath));
                    } else if (config.isList(realPath)) {
                        field.set(this, config.getMessageList(realPath));
                    } else {
                        field.set(this, value);
                    }
                }
            } catch (final IllegalArgumentException ex) {
                final Object[] obj = new Object[] { realPath, field.getType().getName(), value.getClass().getName() };
                Log.log(Level.INFO, "配置节点 {0} 数据类型不匹配 应该为: {1} 但实际为: {2}!", obj);
            } catch (final IllegalAccessException ex) {
                Log.log(Level.SEVERE, "自动注入配置错误!", ex);
            }
        } else if (field.getAnnotation(Nullable.class) == null) {
            Log.warning(String.format("配置节点 %s 丢失!", realPath));
        }
    }
}
