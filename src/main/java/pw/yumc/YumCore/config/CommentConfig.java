package pw.yumc.YumCore.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;

public class CommentConfig extends AbstractConfig {
    // 新增保留注释字段
    protected static final String commentPrefixSymbol = "'注释 ";
    protected static final String commentSuffixSymbol = "': 注释";

    protected static final String fromRegex = "( {0,})(#.*)";
    protected static final Pattern fromPattern = Pattern.compile(fromRegex);

    protected static final String toRegex = "( {0,})(- ){0,}" + "(" + commentPrefixSymbol + ")" + "(#.*)" + "(" + commentSuffixSymbol + ")";
    protected static final Pattern toPattern = Pattern.compile(toRegex);

    protected static final Pattern countSpacePattern = Pattern.compile("( {0,})(- ){0,}(.*)");

    protected static final int commentSplitWidth = 90;

    private static String[] split(final String string, final int partLength) {
        final String[] array = new String[string.length() / partLength + 1];
        for (int i = 0; i < array.length; i++) {
            final int beginIndex = i * partLength;
            int endIndex = beginIndex + partLength;
            if (endIndex > string.length()) {
                endIndex = string.length();
            }
            array[i] = string.substring(beginIndex, endIndex);
        }
        return array;
    }

    @Override
    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        final BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        final StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append(newLine);
            }
        } finally {
            input.close();
        }
        loadFromString(builder.toString());
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        final String[] parts = contents.split(newLine);
        final List<String> lastComments = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        for (final String part : parts) {
            Matcher matcher = fromPattern.matcher(part);
            if (matcher.find()) {
                final String originComment = matcher.group(2);
                final String[] splitComments = split(originComment, commentSplitWidth);
                for (int i = 0; i < splitComments.length; i++) {
                    String comment = splitComments[i];
                    if (i == 0) {
                        comment = comment.substring(1);
                    }
                    comment = COMMENT_PREFIX + comment;
                    lastComments.add(comment.replaceAll("\\.", "．").replaceAll("'", "＇").replaceAll(":", "："));
                }
            } else {
                matcher = countSpacePattern.matcher(part);
                if (matcher.find()) {
                    if (!lastComments.isEmpty()) {
                        for (final String comment : lastComments) {
                            builder.append(matcher.group(1));
                            builder.append(this.checkNull(matcher.group(2)));
                            builder.append(commentPrefixSymbol);
                            builder.append(comment);
                            builder.append(commentSuffixSymbol);
                            builder.append(newLine);
                        }
                        lastComments.clear();
                    }
                }
                builder.append(part);
                builder.append(newLine);
            }
        }
        super.loadFromString(builder.toString());
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
        final String contents = header + dump;
        final StringBuilder savcontent = new StringBuilder();
        final String[] parts = contents.split(newLine);
        for (String part : parts) {
            final Matcher matcher = toPattern.matcher(part);
            if (matcher.find()) {
                if (matcher.groupCount() == 5) {
                    part = this.checkNull(matcher.group(1)) + matcher.group(4);
                }
            }
            savcontent.append(part.replaceAll("．", ".").replaceAll("＇", "'").replaceAll("：", ":"));
            savcontent.append(newLine);
        }
        data = savcontent.toString();
        return data;
    }

    /**
     * 检查字符串
     *
     * @param string
     *            检查字符串
     * @return 返回非null字符串
     */
    private String checkNull(final String string) {
        return string == null ? "" : string;
    }
}