package pw.yumc.YumCore.misc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PasteXcode {
    private final String POST_URL = "http://paste.xcode.ro/";

    public static void main(final String[] args) {
        final PasteXcode p = new PasteXcode();
        final Paste paste = new Paste();
        paste.addLine("异常提交测试!");
        paste.addThrowable(new Throwable());
        System.out.println(p.post(paste));;
    }

    public String post(final PasteXcode.Paste content) {
        return post("YumCore", PasteXcode.Format.JAVA, content);
    }

    public String post(final String name, final PasteXcode.Format format, final PasteXcode.Paste content) {
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
            final byte[] outByte = String.format("paste_user=%s&paste_lang=%s&paste_data=%s&paste_submit=Paste&paste_expire=0", name, format.toString(), content.toString()).getBytes();
            outputStream.write(outByte);
            outputStream.flush();
            outputStream.close();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                result = connection.getHeaderField("Location");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public enum Format {
        JAVA("java"),
        JAVASCRIPT("javascript"),
        HTML("html");

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
}
