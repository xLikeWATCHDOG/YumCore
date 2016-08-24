package pw.yumc.YumCore.config;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import pw.yumc.YumCore.bukkit.Log;

/**
 * 配置自动载入类
 *
 * @since 2016年7月5日 上午8:53:57
 * @author 喵♂呜
 */
public class InjectConfigurationSection extends AbstractInjectConfig {
    protected ConfigurationSection config;

    public InjectConfigurationSection(final ConfigurationSection config) {
        this.config = config;
        inject();
    }

    /**
     * 重载配置文件
     */
    public void reload(final ConfigurationSection config) {
        this.config = config;
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
                    field.set(this, value);
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
