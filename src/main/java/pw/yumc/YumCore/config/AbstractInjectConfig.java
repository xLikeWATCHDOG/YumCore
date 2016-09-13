package pw.yumc.YumCore.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.bukkit.configuration.ConfigurationSection;

import pw.yumc.YumCore.bukkit.Log;

/**
 *
 * @since 2016年7月5日 上午10:11:22
 * @author 喵♂呜
 */
public abstract class AbstractInjectConfig {
    private static final String INJECT_TYPE_ERROR = "配置节点 %s 数据类型不匹配 应该为: %s 但实际为: %s!";
    private static final String INJECT_ERROR = "自动注入配置失败 可能造成插件运行错误 %s: %s!";
    private static final String DATE_PARSE_ERROR = "配置节点 {0} 日期解析失败 格式应该为: {1} 但输入值为: {2}!";
    private static final String PATH_NOT_FOUND = "配置节点 %s 丢失 可能造成插件运行错误!";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    private ConfigurationSection config;

    /**
     * 注入配置数据
     */
    public void inject(final ConfigurationSection config) {
        if (config == null) {
            Log.warning("尝试注入 ConfigurationSection 为 Null 的数据!");
            return;
        }
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
     * 转换字段值类型
     * 
     * @param type
     *            字段类型
     * @param path
     *            配置路径
     * @param value
     *            字段值
     * @return 转换后的值
     * @throws ParseException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private Object convertType(final Class<?> type, final String path, final Object value) throws ParseException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        switch (type.getName()) {
        case "java.util.Date":
            return df.parse((String) value);
        case "java.util.List":
            return config.getList(path);
        default:
            return hanldeDefault(type, path, value);
        }
    }

    /**
     * 默认类型处理流程
     *
     * @param path
     *            路径
     * @param field
     *            字段
     * @param value
     *            值
     * @return 解析后的Value
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private Object hanldeDefault(final Class<?> field, final String path, final Object value) throws IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (InjectConfigurationSection.class.isAssignableFrom(field)) {
            if (config.isConfigurationSection(path)) {
                return field.getConstructor(ConfigurationSection.class).newInstance(config.getConfigurationSection(path));
            }
            Log.w(INJECT_TYPE_ERROR, path, ConfigurationSection.class.getName(), value.getClass().getName());
        }
        return value;
    }

    /**
     * 处理值
     *
     * @param path
     *            路径
     * @param field
     *            字段
     * @param value
     *            值
     */
    private void hanldeValue(final String path, final Field field, Object value) {
        try {
            final Class<?> type = field.getType();
            if (!type.equals(value.getClass())) {
                value = convertType(type, path, value);
            }
            field.set(this, value);
        } catch (final IllegalArgumentException ex) {
            Log.w(INJECT_TYPE_ERROR, path, field.getType().getName(), value.getClass().getName());
            Log.debug(ex);
        } catch (final ParseException e) {
            Log.w(DATE_PARSE_ERROR, path, DATE_FORMAT, value);
        } catch (final InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException | IllegalAccessException ex) {
            Log.w(INJECT_ERROR, ex.getClass().getName(), ex.getMessage());
            Log.debug(ex);
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
        if (value == null && def != null) {
            value = def.value();
        }
        if (value == null) {
            if (field.getAnnotation(Nullable.class) == null) {
                Log.w(PATH_NOT_FOUND, path);
            }
            return;
        }
        hanldeValue(path, field, value);
    }
}
