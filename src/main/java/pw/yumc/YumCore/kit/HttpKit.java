/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.yumc.YumCore.kit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HttpKit
 */
public class HttpKit {

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final String CHARSET = "UTF-8";

    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new HttpKit.TrustAnyHostnameVerifier();

    private HttpKit() {
    }

    /**
     * Get 方法获取HTML
     *
     * @param url
     *            网址
     * @return 网页HTML
     */
    public static String get(final String url) {
        return get(url, null, null);
    }

    /**
     * Get 方法获取HTML
     *
     * @param url
     *            网址
     * @param queryParas
     *            查询参数
     * @return 网页HTML
     */
    public static String get(final String url, final Map<String, String> queryParas) {
        return get(url, queryParas, null);
    }

    /**
     * Get 方法获取HTML
     *
     * @param url
     *            网址
     * @param queryParas
     *            查询参数
     * @param headers
     *            头信息
     * @return 网页HTML
     */
    public static String get(final String url, final Map<String, String> queryParas, final Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), GET, headers);
            conn.connect();
            return readResponseString(conn);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Post 获取网页
     *
     * @param url
     *            网址
     * @param queryParas
     *            参数
     * @param data
     *            数据
     * @return 网页HTML
     */
    public static String post(final String url, final Map<String, String> queryParas, final String data) {
        return post(url, queryParas, data, null);
    }

    /**
     * Post 获取网页
     *
     * @param url
     *            网址
     * @param queryParas
     *            参数
     * @param data
     *            数据
     * @param headers
     *            头信息
     * @return 网页HTML
     */
    public static String post(final String url, final Map<String, String> queryParas, final String data, final Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), POST, headers);
            conn.connect();

            final OutputStream out = conn.getOutputStream();
            out.write(data.getBytes(CHARSET));
            out.flush();
            out.close();

            return readResponseString(conn);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Get 方法获取HTML
     *
     * @param url
     *            网址
     * @param data
     *            查询参数
     * @return 网页HTML
     */
    public static String post(final String url, final String data) {
        return post(url, null, data, null);
    }

    /**
     * Get 方法获取HTML
     *
     * @param url
     *            网址
     * @param data
     *            查询参数
     * @param headers
     *            头信息
     * @return 网页HTML
     */
    public static String post(final String url, final String data, final Map<String, String> headers) {
        return post(url, null, data, headers);
    }

    /**
     * 构建查询串为字符串
     *
     * @param url
     *            网址
     * @param queryParas
     *            参数
     * @return 构建后的地址
     */
    private static String buildUrlWithQueryString(final String url, final Map<String, String> queryParas) {
        if (queryParas == null || queryParas.isEmpty()) {
            return url;
        }

        final StringBuilder sb = new StringBuilder(url);
        boolean isFirst;
        if (url.indexOf("?") == -1) {
            isFirst = true;
            sb.append("?");
        } else {
            isFirst = false;
        }

        for (final Entry<String, String> entry : queryParas.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("&");
            }

            final String key = entry.getKey();
            String value = entry.getValue();
            if (StrKit.notBlank(value)) {
                try {
                    value = URLEncoder.encode(value, CHARSET);
                } catch (final UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }

    /**
     * 获得HTTP链接
     *
     * @param url
     *            地址
     * @param method
     *            方法
     * @param headers
     *            头信息
     * @return HTTP链接
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws KeyManagementException
     */
    private static HttpURLConnection getHttpConnection(final String url, final String method, final Map<String, String> headers) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        final URL _url = new URL(url);
        final HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection) conn).setHostnameVerifier(trustAnyHostnameVerifier);
        }

        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        conn.setConnectTimeout(19000);
        conn.setReadTimeout(19000);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");

        if (headers != null && !headers.isEmpty()) {
            for (final Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return conn;
    }

    /**
     * 获得SSL安全套接字
     *
     * @return 安全套接字工厂
     */
    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            final TrustManager[] tm = { new HttpKit.TrustAnyTrustManager() };
            final SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从连接读取HTML
     *
     * @param conn
     *            HTTP连接
     * @return 字符串
     */
    private static String readResponseString(final HttpURLConnection conn) {
        final StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = conn.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * https 域名校验
     *
     * @author 喵♂呜
     * @since 2016年4月1日 下午10:36:01
     */
    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            return true;
        }
    }

    /**
     * https 证书管理
     *
     * @author 喵♂呜
     * @since 2016年4月1日 下午10:36:05
     */
    private static class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
