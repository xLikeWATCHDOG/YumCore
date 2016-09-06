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
    private static final String DATA_FORMAT_ERROR = "配置节点 {0} 数据类型不匹配 应该为: {1} 但实际为: {2}!";
    private static final String INJECT_ERROR = "自动注入配置错误!";
    private static final String DATE_PARSE_ERROR = "配置节点 {0} 日期解析失败 格式应该为: {1} 但输入值为: {2}!";
    private static final String PATH_NOT_FOUND = "配置节点 %s 丢失!";
    private static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat df = new SimpleDateFormat(DATA_FORMAT);
    private ConfigurationSection config;

    /**
     * 注入配置数据
     */
    public void inject(final ConfigurationSection config) {
        this.config = config;
        for (final Field field : getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || field.getType().isPrimitive()) {
                continue;
            }
            final ConfigNode node = field.getAnnotation(ConfigNode.class);
            String path = field.getName();
            if (node != null && !node.value().isEmpty()) {
                path = node.value();
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
    protected void setField(final String path, final Field field) {
        Object value = config.get(path);
        final Default def = field.getAnnotation(Default.class);
        if (value == null) {
            if (def != null) {
                value = def.value();
            } else {
                if (field.getAnnotation(Nullable.class) == null) {
                    Log.warning(String.format(PATH_NOT_FOUND, path));
                }
                return;
            }
        }
        try {
            final String typeName = field.getType().getName();
            switch (typeName) {
            case "java.util.Date":
                try {
                    value = df.parse((String) value);
                } catch (final ParseException e) {
                    final Object[] obj = new Object[] { path, DATA_FORMAT, value };
                    Log.log(Level.INFO, DATE_PARSE_ERROR, obj);
                }
            case "java.util.List":
                value = config.getList(path);
            default:
            }
            field.set(this, value);
        } catch (final IllegalArgumentException ex) {
            final Object[] obj = new Object[] { path, field.getType().getName(), value.getClass().getName() };
            Log.log(Level.INFO, DATA_FORMAT_ERROR, obj);
        } catch (final IllegalAccessException ex) {
            Log.log(Level.SEVERE, INJECT_ERROR, ex);
        }
    }
}
