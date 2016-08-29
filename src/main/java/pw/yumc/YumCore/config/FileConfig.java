package pw.yumc.YumCore.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import pw.yumc.YumCore.bukkit.Log;

/**
 * 一个继承于 {@link YamlConfiguration} 的配置文件类
 * 强制UTF-8编码处理所有的文件信息
 *
 * @author 喵♂呜
 * @version 1.0
 * @since 2015年11月7日 下午2:36:07
 */
@SuppressWarnings({ "unchecked" })
public class FileConfig extends AbstractConfig {
    protected static final String VERSION = "Version";

    private static final char ALT_COLOR_CHAR = '&';
    private static final String DEFAULT = "config.yml";
    private static final String DATA_FORMANT = "yyyyMMddHHmmss";
    private static final String CONFIG_BACKUP = "配置: %s 已备份为 %s !";
    private static final String CONFIG_UPDATED = "配置: %s 升级成功 版本 %S !";
    private static final String CONFIG_OVERRIDE = "配置: %s 将覆盖原有字段数据...";
    private static final String CONFIG_READ_ERROR = "配置: %s 读取错误...";
    private static final String CONFIG_SAVE_ERROR = "配置: %s 保存错误...";
    private static final String CONFIG_UPDATE_WARN = "配置: %s 版本 %s 过低 正在升级到 %s ...";
    private static final String CONFIG_CREATE_ERROR = "配置: %s 创建失败...";
    private static final String CONFIG_FORMAT_ERROR = "配置: %s 格式错误...";
    private static final String CONFIG_BACKUP_ERROR = "配置: %s 备份失败 异常: %s !";
    private static final String CONFIG_BACKUP_AND_RESET = "配置: %s 格式错误 已备份为 %s 并恢复默认配置!";
    private static final String CONFIG_NOT_FOUND_IN_JAR = "配置: 从插件内部未找到预置的 %s 文件!";
    private static final String CONFIG_READ_COMMENT_ERROR = "配置: 读取文件注释信息失败!";
    private static final String STREAM_NOT_BE_NULL = "数据流不能为 NULL";

    protected File file;

    private CommentConfig commentConfig;

    /**
     * 实例化默认配置文件
     */
    public FileConfig() {
        this(DEFAULT);
    }

    /**
     * 从文件载入配置
     *
     * @param file
     *            配置文件名称
     */
    public FileConfig(final File file) {
        Validate.notNull(file, FILE_NOT_BE_NULL);
        this.file = file;
        init(file);
    }

    /**
     * 从文件载入配置
     *
     * @param plugin
     *            插件
     * @param filename
     *            配置文件名称
     */
    public FileConfig(final String filename) {
        this.file = new File(plugin.getDataFolder(), filename);
        check(file);
        init(file);
    }

    /**
     * 从数据流载入配置文件
     *
     * @param stream
     *            数据流
     */
    private FileConfig(final InputStream stream) {
        init(stream);
    }

    /**
     * 添加到List末尾
     *
     * @param <E>
     *            List内容类型
     * @param path
     *            路径
     * @param obj
     *            对象
     * @return {@link FileConfig}
     */
    public <E> FileConfig addToList(final String path, final E obj) {
        List<E> l = (List<E>) this.getList(path);
        if (null == l) {
            l = new ArrayList<E>();
        }
        l.add(obj);
        return this;
    }

    /**
     * 添加到StringList末尾
     *
     * @param path
     *            路径
     * @param obj
     *            字符串
     * @return {@link FileConfig}
     */
    public FileConfig addToStringList(final String path, final String obj) {
        addToStringList(path, obj, true);
        return this;
    }

    /**
     * 添加到StringList末尾
     *
     * @param path
     *            路径
     * @param obj
     *            字符串
     * @return {@link FileConfig}
     */
    public FileConfig addToStringList(final String path, final String obj, final boolean allowrepeat) {
        List<String> l = this.getStringList(path);
        if (null == l) {
            l = new ArrayList<>();
        }
        if (allowrepeat || !l.contains(obj)) {
            l.add(obj);
        }
        this.set(path, l);
        return this;
    }

    /**
     * 获得已颜色转码的文本
     *
     * @param cfgmsg
     *            待转码的List
     * @return 颜色转码后的文本
     */
    public List<String> getColorList(final List<String> cfgmsg) {
        final List<String> message = new ArrayList<String>();
        if (cfgmsg == null) {
            return null;
        }
        for (final String msg : cfgmsg) {
            message.add(ChatColor.translateAlternateColorCodes('&', msg));
        }
        return message;
    }

    /**
     * 获得配置文件名称
     *
     * @return 配置文件名称
     */
    public String getConfigName() {
        return file.getName();
    }

    /**
     * 获得Location
     *
     * @param key
     *            键
     * @return {@link Location}
     */
    public Location getLocation(final String key) {
        return getLocation(key, null);
    }

    /**
     * 获得Location
     *
     * @param key
     *            键
     * @param def
     *            默认地点
     * @return {@link Location}
     */
    public Location getLocation(final String path, final Location def) {
        final Object val = get(path, def);
        return val instanceof Location ? (Location) val : def;
    }

    /**
     * 获得已颜色转码的文本
     *
     * @param path
     *            配置路径
     * @return 颜色转码后的文本
     */
    public String getMessage(final String path) {
        return getMessage(path, null);
    }

    /**
     * 获得已颜色转码的文本
     *
     * @param path
     *            配置路径
     * @param def
     *            默认文本
     * @return 颜色转码后的文本
     */
    public String getMessage(final String path, final String def) {
        String message = this.getString(path, def);
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        return message;
    }

    /**
     * 获得已颜色转码的文本
     *
     * @param path
     *            配置路径
     * @return 颜色转码后的文本
     */
    public List<String> getMessageList(final String path) {
        final List<String> cfgmsg = this.getStringList(path);
        if (cfgmsg == null) {
            return null;
        }
        for (int i = 0; i < cfgmsg.size(); i++) {
            cfgmsg.set(i, ChatColor.translateAlternateColorCodes(ALT_COLOR_CHAR, cfgmsg.get(i)));
        }
        return cfgmsg;
    }

    /**
     * 获得字符串数组
     *
     * @param path
     *            配置路径
     * @return 字符串数组
     */
    public String[] getStringArray(final String path) {
        return this.getStringList(path).toArray(new String[0]);
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        try {
            commentConfig = new CommentConfig();
            commentConfig.loadFromString(contents);
        } catch (final Exception e) {
            Log.debug(CONFIG_READ_COMMENT_ERROR);
            commentConfig = null;
        }
        super.loadFromString(contents);
    }

    /**
     * 重新载入配置文件
     *
     * @return 是否载入成功
     */
    public boolean reload() {
        return init(file) != null;
    }

    /**
     * 从List移除对象
     *
     * @param <E>
     *            List内容对象类型
     * @param path
     *            路径
     * @param obj
     *            对象
     * @return {@link FileConfig}
     */
    public <E> FileConfig removeFromList(final String path, final E obj) {
        final List<E> l = (List<E>) this.getList(path);
        if (null != l) {
            l.remove(obj);
        }
        return this;
    }

    /**
     * 从StringList移除对象
     *
     * @param path
     *            路径
     * @param obj
     *            对象
     * @return {@link FileConfig}
     */
    public FileConfig removeFromStringList(final String path, final String obj) {
        final List<String> l = this.getStringList(path);
        if (null != l) {
            l.remove(obj);
        }
        this.set(path, obj);
        return this;
    }

    /**
     * 快速保存配置文件
     *
     * @return 是否成功
     */
    public boolean save() {
        try {
            this.save(file);
            return true;
        } catch (final IOException e) {
            Log.warning(String.format(CONFIG_SAVE_ERROR, file.getName()));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void save(final File file) throws IOException {
        Validate.notNull(file, FILE_NOT_BE_NULL);
        if (commentConfig != null) {
            data = commentConfig.saveToString();
        } else {
            data = saveToString();
        }
        super.save(file);
    }

    @Override
    public void set(final String path, final Object value) {
        if (commentConfig != null) {
            commentConfig.set(path, value);
        }
        super.set(path, value);
    }

    /**
     * 检查配置文件版本
     *
     * @param newcfg
     *            新配置文件
     * @param oldcfg
     *            旧配置文件
     * @return 是否需要升级
     * @throws IOException
     */
    private boolean needUpdate(final FileConfig newcfg, final FileConfig oldcfg) throws IOException {
        final String newver = newcfg.getString(VERSION);
        return newver != null && !newver.equalsIgnoreCase(oldcfg.getString(VERSION));
    }

    /**
     * 从Jar保存配置文件
     */
    private void saveFromJar() {
        if (plugin != null && file != null) {
            try {
                final String filename = file.getName();
                final InputStream filestream = plugin.getResource(file.getName());
                final String errFileName = this.getErrName(filename);
                file.renameTo(new File(file.getParent(), errFileName));
                if (filestream == null) {
                    file.createNewFile();
                } else {
                    plugin.saveResource(filename, true);
                }
                Log.warning(String.format(CONFIG_BACKUP_AND_RESET, filename, errFileName));
            } catch (final IOException | IllegalArgumentException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            Log.warning(String.format(CONFIG_NOT_FOUND_IN_JAR, file != null ? file.getName() : ""));
        }
    }

    protected void backupConfig(final FileConfig oldcfg) {
        final String filename = oldcfg.getConfigName();
        try {
            final String newCfgName = this.getBakName(filename);
            final File newcfg = new File(file.getParent(), newCfgName);
            oldcfg.save(newcfg);
            Log.warning(String.format(CONFIG_BACKUP, filename, newCfgName));
        } catch (final IOException e) {
            Log.warning(String.format(CONFIG_BACKUP_ERROR, filename, e.getMessage()));
        }
    }

    /**
     * 检查配置文件
     *
     * @param file
     *            配置文件
     */
    protected void check(final File file) {
        final String filename = file.getName();
        final InputStream stream = plugin.getResource(filename);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                if (stream == null) {
                    file.createNewFile();
                } else {
                    plugin.saveResource(filename, true);
                }
            } else {
                if (stream == null) {
                    return;
                }
                final FileConfig newcfg = new FileConfig(stream);
                final FileConfig oldcfg = new FileConfig(file);
                if (needUpdate(newcfg, oldcfg)) {
                    backupConfig(oldcfg);
                    updateConfig(newcfg, oldcfg).save(file);
                }
            }
        } catch (final IOException e) {
            Log.warning(String.format(CONFIG_CREATE_ERROR, filename));
        }
    }

    protected String getBakName(final String cfgname) {
        return cfgname + "." + getStringDate(DATA_FORMANT) + ".bak";
    }

    protected String getErrName(final String cfgname) {
        return cfgname + "." + getStringDate(DATA_FORMANT) + ".err";
    }

    /**
     * 获取现在时间
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    protected String getStringDate(String format) {
        format = format == null ? format : "yyyy-MM-dd HH:mm:ss";
        final Date currentTime = new Date();
        return new SimpleDateFormat(format).format(currentTime);
    }

    /**
     * 初始化FileConfig
     *
     * @param file
     *            配置文件
     * @return FileConfig
     */
    protected FileConfig init(final File file) {
        Validate.notNull(file, FILE_NOT_BE_NULL);
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
            init(stream);
        } catch (final FileNotFoundException e) {
        }
        return this;
    }

    /**
     * 初始化FileConfig
     *
     * @param stream
     *            输入流
     * @return FileConfig
     */
    protected FileConfig init(final InputStream stream) {
        Validate.notNull(stream, STREAM_NOT_BE_NULL);
        try {
            this.load(new InputStreamReader(stream, UTF_8));
        } catch (final InvalidConfigurationException | IllegalArgumentException ex) {
            if (file == null) {
                throw new IllegalArgumentException(ex);
            }
            Log.warning(String.format(CONFIG_FORMAT_ERROR, file.getName()));
            Log.warning(ex.getMessage());
            saveFromJar();
        } catch (final IOException ex) {
            if (file == null) {
                throw new IllegalStateException(ex);
            }
            Log.warning(String.format(CONFIG_READ_ERROR, file.getName()));
        }
        return this;
    }

    /**
     * @param newCfg
     * @param oldCfg
     *
     * @return
     */
    protected FileConfig updateConfig(final FileConfig newCfg, final FileConfig oldCfg) {
        return updateConfig(newCfg, oldCfg, false);
    }

    /**
     * 更新配置文件
     *
     * @param newCfg
     *            新的配置文件
     * @param oldCfg
     *            老的配置文件
     * @param force
     *            是否强制更新
     * @return 更新以后的配置文件
     */
    protected FileConfig updateConfig(final FileConfig newCfg, final FileConfig oldCfg, final boolean force) {
        final String filename = oldCfg.getConfigName();
        final String newver = newCfg.getString(VERSION);
        final String oldver = oldCfg.getString(VERSION);
        final Set<String> oldConfigKeys = oldCfg.getKeys(true);
        Log.warning(String.format(CONFIG_UPDATE_WARN, filename, oldver, newver));
        // 保留版本字段 不更新
        oldConfigKeys.remove(VERSION);
        // 强制更新 去除新版本存在的字段
        if (force) {
            Log.warning(String.format(CONFIG_OVERRIDE, filename));
            oldConfigKeys.removeAll(newCfg.getKeys(true));
        }
        // 复制旧的数据
        for (final String string : oldConfigKeys) {
            newCfg.set(string, oldCfg.get(string));
        }
        Log.info(String.format(CONFIG_UPDATED, filename, newver));
        return newCfg;
    }
}
