package pw.yumc.YumCore.paste;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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
        final PasteContent paste = new PasteContent();
        paste.addLine("异常提交测试!");
        paste.addThrowable(new Throwable());
        System.out.println(p.post(paste));;
    }

    public String post(final PasteContent content) {
        return post("", PasteFormat.JAVA, Pastebin.Private.UNLISTED, content);
    }

    public String post(final String name, final PasteFormat format, final Pastebin.Private level, final PasteContent content) {
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
