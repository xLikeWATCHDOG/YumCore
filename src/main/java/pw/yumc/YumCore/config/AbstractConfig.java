package pw.yumc.YumCore.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.io.Files;

/**
 * 抽象配置文件
 *
 * @since 2016年3月12日 下午4:46:45
 * @author 喵♂呜
 */
public class AbstractConfig extends YamlConfiguration {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    protected final DumperOptions yamlOptions = new DumperOptions();
    protected final Representer yamlRepresenter = new YamlRepresenter();
    protected final Yaml yamlz = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    protected String data;

    @Override
    public void load(final File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "文件不能为null");
        final FileInputStream stream = new FileInputStream(file);
        load(new InputStreamReader(stream, UTF_8));
    }

    @Override
    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        final BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        final StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }
        loadFromString(builder.toString());
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "内容不能为 null");
        Map<?, ?> input;
        try {
            input = (Map<?, ?>) yamlz.load(contents);
        } catch (final YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (final ClassCastException e) {
            throw new InvalidConfigurationException("顶层键值必须是Map.");
        }
        final String header = parseHeader(contents);
        if (header.length() > 0) {
            options().header(header);
        }
        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    @Override
    public void save(final File file) throws IOException {
        Validate.notNull(file, "文件不得为 null");
        Files.createParentDirs(file);
        final Writer writer = new OutputStreamWriter(new FileOutputStream(file), UTF_8);
        try {
            writer.write(data);
        } finally {
            writer.close();
        }
    }

    @Override
    public String saveToString() {
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        final String header = buildHeader();
        String dump = yamlz.dump(getValues(false));
        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }
        data = header + dump;
        return data;
    }
}
