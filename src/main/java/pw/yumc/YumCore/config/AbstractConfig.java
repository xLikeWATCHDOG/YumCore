package pw.yumc.YumCore.config;

import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;
import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.bukkit.P;
import pw.yumc.YumCore.config.yaml.BukkitConstructor;
import pw.yumc.YumCore.config.yaml.BukkitRepresenter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 抽象配置文件
 *
 * @author 喵♂呜
 * @since 2016年3月12日 下午4:46:45
 */
public abstract class AbstractConfig extends YamlConfiguration {
    private static String CONTENT_NOT_BE_NULL = "内容不能为 null";

    protected static Charset UTF_8 = Charset.forName("UTF-8");

    protected static String FILE_NOT_BE_NULL = "文件不能为 NULL";
    protected static String CREATE_NEW_CONFIG = "配置: 创建新的文件 %s ...";
    protected static String newLine = "\n";

    protected static Plugin plugin = P.instance;

    protected DumperOptions yamlOptions = new DumperOptions();
    protected Representer yamlRepresenter = BukkitRepresenter.DEFAULT;
    protected Yaml yamlz = new Yaml(BukkitConstructor.DEFAULT, yamlRepresenter, yamlOptions);

    /**
     * 配置文件内容MAP
     */
    protected Map contentsMap;

    /**
     * 配置内容字符串
     */
    protected String data;

    /**
     * @return 获得配置内容
     */
    public Map getContentMap() {
        return contentsMap;
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, FILE_NOT_BE_NULL);
        FileInputStream stream = new FileInputStream(file);
        load(new InputStreamReader(stream, UTF_8));
    }

    @Override
    public void load(Reader reader) throws IOException, InvalidConfigurationException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append(newLine);
            }
        }
        loadFromString(builder.toString());
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, CONTENT_NOT_BE_NULL);
        try {
            contentsMap = (Map) yamlz.load(contents);
        } catch (YAMLException | ClassCastException e) {
            throw new InvalidConfigurationException(e);
        }
        String header = parseHeader(contents);
        if (header.length() > 0) {
            options().header(header);
        }
        if (contentsMap != null) {
            convertMapsToSections(contentsMap, this);
        }
    }

    protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    protected String parseHeader(String input) {
        String[] lines = input.split("\r?\n", -1);
        StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for (int i = 0; (i < lines.length) && (readingHeader); i++) {
            String line = lines[i];

            if (line.startsWith(COMMENT_PREFIX)) {
                if (i > 0) {
                    result.append("\n");
                }

                if (line.length() > COMMENT_PREFIX.length()) {
                    result.append(line.substring(COMMENT_PREFIX.length()));
                }

                foundHeader = true;
            } else if ((foundHeader) && (line.length() == 0)) {
                result.append("\n");
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    @Override
    protected String buildHeader() {
        String header = options().header();
        if (header == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if ((startedHeader) || (lines[i].length() != 0)) {
                builder.insert(0, lines[i]);
                builder.insert(0, COMMENT_PREFIX);
                startedHeader = true;
            }
        }

        return builder.toString();
    }

    @Override
    public void save(File file) throws IOException {
        Validate.notNull(file, FILE_NOT_BE_NULL);
        Files.createParentDirs(file);
        if (!file.exists()) {
            file.createNewFile();
            Log.i(CREATE_NEW_CONFIG, file.toPath());
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8)) {
            writer.write(data);
        }
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String header = buildHeader();
        String dump = yamlz.dump(getValues(false));
        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }
        data = header + dump;
        return data;
    }
}
