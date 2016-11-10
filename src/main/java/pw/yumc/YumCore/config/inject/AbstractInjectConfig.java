package pw.yumc.YumCore.config.inject;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.commands.exception.CommandParseException;
import pw.yumc.YumCore.config.annotation.ConfigNode;
import pw.yumc.YumCore.config.annotation.Default;
import pw.yumc.YumCore.config.annotation.Nullable;
import pw.yumc.YumCore.config.annotation.ReadOnly;
import pw.yumc.YumCore.config.exception.ConfigParseException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 抽象注入配置
 *
 * @author 喵♂呜
 * @since 2016年7月5日 上午10:11:22
 */
public abstract class AbstractInjectConfig {
    private static String INJECT_TYPE_ERROR = "配置节点 %s 数据类型不匹配 应该为: %s 但实际为: %s!";
    private static String INJECT_ERROR = "自动注入配置失败 可能造成插件运行错误 %s: %s!";
    private static String PATH_NOT_FOUND = "配置节点 %s 丢失 将使用默认值!";
    private ConfigurationSection config;

    /**
     * 添加默认值
     *
     * @param field
     *            字段
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private void applyDefault(Field field) throws IllegalArgumentException, IllegalAccessException {
        Object value = null;
        switch (field.getType().getName()) {
        case "java.util.List":
            value = new ArrayList<>();
            break;
        case "java.util.Map":
            value = new HashMap<>();
            break;
        }
        field.set(this, value);
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
    private Object convertType(Class<?> type, String path, Object value) throws CommandParseException, IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Object result = InjectParse.parse(type, config, path);
        return result == null ? hanldeDefault(type, path, value) : result;
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
    private Object hanldeDefault(Class<?> field, String path, Object value) throws IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (InjectConfigurationSection.class.isAssignableFrom(field)) {
            if (config.isConfigurationSection(path)) { return field.getConstructor(ConfigurationSection.class).newInstance(config.getConfigurationSection(path)); }
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
     * @throws ParseException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void hanldeValue(String path, Field field, Object value) throws IllegalAccessException, IllegalArgumentException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, ConfigParseException {
        Class<?> type = field.getType();
        if (!type.equals(value.getClass())) {
            value = convertType(type, path, value);
        }
        if (type.equals(String.class)) {
            value = ChatColor.translateAlternateColorCodes('&', String.valueOf(value));
        }
        field.set(this, value);
        Log.d("设置字段 %s 为 %s ", field.getName(), value);
    }

    /**
     * 配置注入后的初始化操作(对象初始化也要在此处)
     */
    protected void init() {
    }

    /**
     * 注入配置数据
     *
     * @param config
     *            配置区
     */
    public void inject(ConfigurationSection config) {
        inject(config, false);
        init();
    }

    /**
     * 注入配置数据
     *
     * @param config
     *            配置区
     * @param save
     *            是否为保存
     */
    public void inject(ConfigurationSection config, boolean save) {
        if (config == null) {
            Log.w("尝试%s ConfigurationSection 为 Null 的数据!", save ? "保存" : "读取");
            return;
        }
        this.config = config;
        for (Field field : getClass().getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || field.getType().isPrimitive()) {
                continue;
            }
            ConfigNode node = field.getAnnotation(ConfigNode.class);
            String path = field.getName();
            if (node != null && !node.value().isEmpty()) {
                path = node.value();
            }
            field.setAccessible(true);
            if (save) {
                setConfig(path, field);
            } else {
                setField(path, field);
            }
        }
    }

    /**
     * 自动化保存
     *
     * @param config
     *            配置文件区
     */
    public ConfigurationSection save(ConfigurationSection config) {
        inject(config, true);
        return config;
    }

    /**
     * 通用保存流程
     *
     * @param path
     *            配置路径
     * @param field
     *            字段
     */
    protected void setConfig(String path, Field field) {
        try {
            if (field.getAnnotation(ReadOnly.class) == null) {
                config.set(path, field.get(this));
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Log.w(INJECT_ERROR, e.getClass().getName(), e.getMessage());
            Log.debug(e);
        }
    }

    /**
     * 通用读取流程
     *
     * @param path
     *            配置路径
     * @param field
     *            字段
     */
    protected void setField(String path, Field field) {
        Object value = config.get(path);
        try {
            Default def = field.getAnnotation(Default.class);
            if (value == null && def != null) {
                value = def.value();
            }
            if (value == null) {
                if (field.getAnnotation(Nullable.class) == null) {
                    Log.w(PATH_NOT_FOUND, path);
                    applyDefault(field);
                }
                return;
            }
            hanldeValue(path, field, value);
        } catch (IllegalArgumentException ex) {
            Log.w(INJECT_TYPE_ERROR, path, field.getType().getName(), value != null ? value.getClass().getName() : "空指针");
            Log.debug(ex);
        } catch (ConfigParseException e) {
            Log.w(e.getMessage());
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException | IllegalAccessException ex) {
            Log.w(INJECT_ERROR, ex.getClass().getName(), ex.getMessage());
            Log.debug(ex);
        }
    }
}