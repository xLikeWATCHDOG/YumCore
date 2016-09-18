package pw.yumc.YumCore.paste;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Pastebin {
    private final String POST_URL = "http://pastebin.com/api/api_post.php";
    private final String API_KEY;

    public Pastebin() {
        this.API_KEY = "0e7d92011945cbcc1e884ab6e3e75e69";
    }

    public Pastebin(final String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public static void main(final String[] args) {
        final Pastebin p = new Pastebin();
        final Paste paste = new Paste();
        paste.addLine("异常提交测试!");
        paste.addThrowable(new Throwable());
        System.out.println(p.post(paste));;
    }

    public String post(final Pastebin.Paste content) {
        return post("", Pastebin.Format.JAVA, Pastebin.Private.UNLISTED, content);
    }

    public String post(final String name, final Pastebin.Format format, final Pastebin.Private level, final Pastebin.Paste content) {
        String result = "Failed to post!";
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(POST_URL).openConnection();
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);
            final OutputStream outputStream = connection.getOutputStream();
            final byte[] outByte = ("api_option=paste&api_dev_key="
                    + URLEncoder.encode(this.API_KEY, "utf-8")
                    + "&api_paste_code="
                    + URLEncoder.encode(content.toString(), "utf-8")
                    + "&api_paste_private="
                    + URLEncoder.encode(level.getLevel(), "utf-8")
                    + "&api_paste_name="
                    + URLEncoder.encode(name, "utf-8")
                    + "&api_paste_expire_date="
                    + URLEncoder.encode("N", "utf-8")
                    + "&api_paste_format="
                    + URLEncoder.encode(format.toString(), "utf-8")
                    + "&api_user_key="
                    + URLEncoder.encode("", "utf-8")).getBytes();
            outputStream.write(outByte);
            outputStream.flush();
            outputStream.close();
            final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final StringBuffer request = new StringBuffer();
            String temp;
            while ((temp = br.readLine()) != null) {
                request.append(temp);
                request.append("\r\n");
            }
            br.close();
            result = request.toString().trim();
            if (!result.contains("http://")) {
                result = "Failed to post! (returned result: " + result;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public enum Format {
        JAVA("java"),
        YAML("yaml");

        String format;

        private Format(final String format) {
            this.format = format;
        }

        @Override
        public String toString() {
            return format;
        }
    }

    public static class Paste {
        private final static String errN = "异常名称: %s";
        private final static String errM = "异常说明: %s";
        private final static String errInfo = "简易错误信息如下:";
        private final static String errStackTrace = "    位于 %s.%s(%s:%s)";
        private final List<String> TEXT = new ArrayList<>();

        public void addFile(final File file) throws IOException {
            if (file == null) {
                throw new IllegalArgumentException("文件不得为Null!");
            }
            addLines(Files.readAllLines(file.toPath()));
        }

        public void addLine(final String str) {
            this.TEXT.add(str);
        }

        public void addLines(final List<String> str) {
            this.TEXT.addAll(str);
        }

        public void addThrowable(final Throwable e) {
            Throwable temp = e;
            while (temp.getCause() != null) {
                temp = temp.getCause();
            }
            TEXT.add(String.format(errN, e.getClass().getName()));
            TEXT.add(String.format(errM, e.getMessage()));
            TEXT.add(errInfo);
            for (final StackTraceElement ste : e.getStackTrace()) {
                TEXT.add(String.format(errStackTrace, ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber() < 0 ? "未知" : ste.getLineNumber()));
            }
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder();
            for (final String str : TEXT) {
                text.append(str + '\n');
            }
            return text.toString();
        }
    }

    public enum Private {
        PUBLIC(0),
        UNLISTED(1),
        PRIVATE(2);

        int level;

        private Private(final int level) {
            this.level = level;
        }

        public String getLevel() {
            return String.valueOf(level);
        }
    }
}
