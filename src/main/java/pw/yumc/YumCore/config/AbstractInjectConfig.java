package pw.yumc.YumCore.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import pw.yumc.YumCore.bukkit.Log;

/**
 *
 * @since 2016年7月5日 上午10:11:22
 * @author 喵♂呜
 */
public abstract class AbstractInjectConfig {
    /**
     * 注入配置数据
     */
    public void inject() {
        for (final Field field : getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || field.getType().isPrimitive()) {
                continue;
            }
            final ConfigNode node = field.getAnnotation(ConfigNode.class);
            String path = field.getName();
            if (node != null && !node.path().isEmpty()) {
                path = node.path();
            }
            field.setAccessible(true);
            setField(path, field);
        }
    }

    /**
     * 通用解析流程
     *
     * @param path
     *            配置路径
     * @param field
     *            字段
     * @return 是否解析成功
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected boolean commonParse(final ConfigurationSection config, final String path, final Field field) throws IllegalArgumentException, IllegalAccessException {
        final String typeName = field.getType().getName();
        switch (typeName) {
        case "java.util.Date":
            final String format = "yyyy-MM-dd HH:mm:ss";
            final String value = config.getString(path);
            try {
                field.set(this, new SimpleDateFormat(format).parse(value));
            } catch (final ParseException e) {
                final Object[] obj = new Object[] { path, format, value };
                Log.log(Level.INFO, "配置节点 {0} 日期解析失败 格式应该为: {1} 但输入值为: {2}!", obj);
            }
            return true;
        case "java.util.List":
            field.set(this, config.getList(path));
            return true;
        default:
            return false;
        }
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
    protected abstract void setField(final String path, final Field field) throws IllegalArgumentException;
}
