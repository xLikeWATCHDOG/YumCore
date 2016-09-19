package pw.yumc.YumCore.paste;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PasteXcode {
    private final String POST_URL = "http://paste.xcode.ro/";

    public static void main(final String[] args) {
        final PasteXcode p = new PasteXcode();
        final PasteContent paste = new PasteContent();
        paste.addLine("异常提交测试!");
        paste.addThrowable(new Throwable());
        System.out.println(p.post(paste));;
    }

    public String post(final PasteContent content) {
        return post("YumCore", PasteFormat.JAVA, content);
    }

    public String post(final String name, final PasteFormat format, final PasteContent content) {
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
}
