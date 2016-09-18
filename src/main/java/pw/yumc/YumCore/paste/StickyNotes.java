package pw.yumc.YumCore.paste;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * 数据提交
 *
 * @since 2016年9月18日 下午6:57:34
 * @author 喵♂呜
 */
public class StickyNotes {
    private final static String DOMAIN = "http://paste.yumc.pw";
    private final String POST_URL = DOMAIN + "/api/json/create";
    private final String VIEW_URL = DOMAIN + "/%s/%s";

    public static void main(final String[] args) {
        final StickyNotes p = new StickyNotes();
        final Paste paste = new Paste();
        paste.addLine("异常提交测试!");
        paste.addThrowable(new Throwable());
        System.out.println(p.post(StickyNotes.Expire.HalfHour, paste));
        ;
    }

    /**
     * 上传数据
     *
     * @param expire
     *            过期时间
     * @param content
     *            内容
     * @return 地址
     */
    public String post(final StickyNotes.Expire expire, final StickyNotes.Paste content) {
        return post("YumCore-" + System.currentTimeMillis(), StickyNotes.Format.JAVA, expire, content);
    }

    /**
     * 上传数据
     *
     * @param content
     *            内容
     * @return 地址
     */
    public String post(final StickyNotes.Paste content) {
        return post("YumCore-" + System.currentTimeMillis(), StickyNotes.Format.JAVA, StickyNotes.Expire.Never, content);
    }

    /**
     * 上传数据
     *
     * @param title
     *            标题
     * @param format
     *            格式
     * @param expire
     *            过期时间
     * @param content
     *            内容
     * @return 地址
     */
    public String post(final String title, final StickyNotes.Format format, final StickyNotes.Expire expire, final StickyNotes.Paste content) {
        return post(title, format.toString(), expire.getExpire(), content.toString());
    }

    /**
     * 上传数据
     *
     * @param title
     *            标题
     * @param format
     *            格式
     * @param expire
     *            过期时间
     * @param content
     *            内容
     * @return 地址
     */
    public String post(final String title, final String format, final int expire, final String content) {
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
            final byte[] outByte = String.format("title=%s&language=%s&expire=%s&data=%s", title, format, expire, content).getBytes();
            outputStream.write(outByte);
            outputStream.flush();
            outputStream.close();
            final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            final StringBuffer request = new StringBuffer();
            String temp;
            while ((temp = br.readLine()) != null) {
                request.append(temp);
            }
            br.close();
            result = request.toString().trim();
            JSONObject object = (JSONObject) JSONValue.parse(result);
            object = (JSONObject) object.get("result");
            if (object.containsKey("error")) {
                return object.get("error").toString();
            }
            return String.format(VIEW_URL, object.get("id"), object.get("hash"));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 过期时间
     *
     * @since 2016年9月18日 下午7:00:09
     * @author 喵♂呜
     */
    public enum Expire {
        HalfHour(1800),
        Hour(21600),
        Day(86400),
        Week(604800),
        Mouth(2592000),
        Year(31536000),
        Never(31536000);

        int expire;

        private Expire(final int expire) {
            this.expire = expire;
        }

        public int getExpire() {
            return expire;
        }
    }

    /**
     * 代码格式
     *
     * @since 2016年9月18日 下午7:00:15
     * @author 喵♂呜
     */
    public enum Format {
        JAVA("java"),
        JAVASCRIPT("javascript"),
        HTML("html"),
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

    /**
     * 数据组装
     *
     * @since 2016年9月18日 下午7:00:21
     * @author 喵♂呜
     */
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
            addLines(Files.readAllLines(file.toPath(), Charset.forName("UTF-8")));
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
}
